"""Scheduled Gujeuk full dumps and publication to daily Notion pages."""
import argparse
import datetime as dt
import fcntl
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

from backup_core import BackupError, KST, MIB, MySQL, Notion, fingerprint, now, save_json, secure_directory


def read_json(path, default=None):
    return json.loads(path.read_text()) if path.exists() else default


def rich(text):
    return [{"type": "text", "text": {"content": text}}]


def block(kind, text):
    return {"object": "block", "type": kind, kind: {"rich_text": rich(text)}}


def page_template():
    return [block("heading_2", "전체 DUMP 파일"), block("heading_2", "시간대별 DUMP 파일"),
            *[block("heading_3", "%02d:00" % hour) for hour in range(1, 20)],
            block("heading_2", "오류 내용"), block("paragraph", "없음")]


def capture_slot(mode, moment):
    if mode == "bootstrap":
        return "bootstrap"
    if mode == "hourly" and 1 <= moment.hour <= 19 and (moment.hour, moment.minute) < (19, 30):
        return "hourly-%02d" % moment.hour
    if mode == "dump" and (moment.hour, moment.minute) >= (19, 30):
        return "final"


class Repository:
    def __init__(self, cfg, source=None):
        self.cfg, self.root = cfg, Path(cfg["backup_directory"])
        secure_directory(self.root)
        self.source = source or MySQL(cfg)
        self.state_path = self.root / "state.json"
        self.state = read_json(self.state_path, {})

    def directory(self, day):
        path = self.root / day
        secure_directory(path)
        return path

    def archive(self, manifest, meta):
        return manifest.parent / meta["filename"]

    def snapshot(self, moment, slot):
        day = moment.date().isoformat()
        directory = self.directory(day)
        manifest = directory / (slot + ".json")
        if manifest.exists():
            return manifest
        stats = self.source.check()
        if shutil.disk_usage(self.root).free < max(
                self.cfg.get("minimum_free_bytes", 512*MIB), 2*stats["estimated_bytes"]):
            raise BackupError("Insufficient disk space")
        name = "gujeuk-prod_%s_%s_%s.sql.gz" % (day, moment.strftime("%H%M%S"), slot)
        final, partial = directory / name, directory / (name + ".partial")
        try:
            self.source.dump(partial)
            checked = fingerprint(partial)
            partial.replace(final)
            save_json(manifest, {"kind": "dump", "slot": slot, "date": day, "filename": name,
                "started_at": moment.isoformat(), "completed_at": now().isoformat(),
                "database": self.cfg["database"], "source_metadata": stats, **checked})
            return manifest
        finally:
            partial.unlink(missing_ok=True)

    def manifests(self):
        return sorted(self.root.glob("????-??-??/*.json"))


class Destination(Notion):
    def page(self, day):
        return self.find_page(day.replace("-", ".") + " / [PROD]Gujeuk_DB", page_template(),
            {"백업일": {"date": {"start": day}}, "환경": {"select": {"name": "prod"}}})

    def publish(self, repository, manifest, limit):
        meta = read_json(manifest)
        page_id = self.page(meta["date"])
        children = self.blocks(page_id)
        attached = any(b["type"] == "file" and b["file"].get("name") == meta["filename"]
                       for b in children)
        if not attached:
            heading = ("전체 DUMP 파일" if meta["slot"] == "final" else
                       meta["slot"][-2:] + ":00" if meta["slot"].startswith("hourly-") else
                       "시간대별 DUMP 파일")
            anchor = next(b["id"] for b in children if b["type"].startswith("heading_") and
                          "".join(t.get("plain_text", t.get("text", {}).get("content", ""))
                          for t in b[b["type"]]["rich_text"]) == heading)
            upload_id = self.upload(repository.archive(manifest, meta), meta,
                                    lambda: save_json(manifest, meta), limit)
            self.request("PATCH", "/blocks/" + page_id + "/children", {
                "position": {"type": "after_block", "after_block": {"id": anchor}},
                "children": [{"object": "block", "type": "file", "file": {
                    "type": "file_upload", "file_upload": {"id": upload_id},
                    "name": meta["filename"]}}]})
        meta.update(notion_attached=True, page_id=page_id, attached_at=now().isoformat(),
                    summary_synced=False)
        save_json(manifest, meta)

    def update_day(self, repository, day):
        items = [read_json(p) for p in repository.directory(day).glob("*.json")]
        uploaded = [m for m in items if m.get("notion_attached")]
        expected = {"hourly-%02d" % h for h in range(1, 20)} | {"final"}
        finished = day < now().date().isoformat() or (now().hour, now().minute) >= (19, 30)
        missing = sorted(expected - {m["slot"] for m in uploaded}) if finished else []
        errors = [slot + ": " + e.get("message", "덤프 생성 실패") for slot, e in
                  repository.state.get("capture_errors", {}).get(day, {}).items()]
        if missing:
            errors.append("백업 누락: " + ", ".join(
                "19:30(최종)" if slot == "final" else slot[-2:] + ":00" for slot in missing))
        if any(not m.get("notion_attached") for m in items):
            errors.append("Notion에 올리지 못한 파일은 EC2에 보관 중입니다.")
        props = {"검증": {"select": {"name": "복원 확인" if uploaded and all(
            m.get("restore_verified") for m in uploaded) else "무결성 확인" if uploaded else "미검증"}}}
        if uploaded:
            props["마지막 백업 시각"] = {"date": {"start": max(m["started_at"] for m in uploaded)}}
        for prop, final in (("시간별 덤프 파일", False), ("최종 덤프 파일", True)):
            props[prop] = {"files": [{"type": "file_upload", "name": m["filename"],
                "file_upload": {"id": m["upload_id"]}} for m in uploaded if (m["slot"] == "final") == final]}
        page_id = self.page(day)
        self.request("PATCH", "/pages/" + page_id, {"properties": props})
        children = self.blocks(page_id)
        index = next(i for i, b in enumerate(children) if b["type"] == "heading_2" and
                     "".join(t.get("plain_text", t.get("text", {}).get("content", ""))
                     for t in b["heading_2"]["rich_text"]) == "오류 내용")
        self.request("PATCH", "/blocks/" + children[index + 1]["id"], {
            "paragraph": {"rich_text": rich("\n".join(errors) or "없음")}})
        for manifest in repository.directory(day).glob("*.json"):
            meta = read_json(manifest)
            meta["summary_synced"] = True
            save_json(manifest, meta)


def publish_pending(cfg, repository, day):
    destination = Destination(cfg)
    limit = destination.preflight()
    destination.page(day)
    touched, failed = {day}, []
    for manifest in repository.manifests():
        meta = read_json(manifest)
        if not meta.get("summary_synced"):
            touched.add(meta["date"])
        if not meta.get("notion_attached"):
            try:
                destination.publish(repository, manifest, limit)
            except BackupError:
                failed.append(manifest)
    previous = (dt.date.fromisoformat(day) - dt.timedelta(days=1)).isoformat()
    if (repository.root / previous).exists():
        touched.add(previous)
    touched.update(repository.state.get("capture_errors", {}))
    for date in sorted(touched):
        destination.update_day(repository, date)
    if failed:
        raise BackupError("Upload pending; local dumps are preserved")


def run(cfg, mode, moment=None):
    repository = Repository(cfg)
    with (repository.root / ".lock").open("a+") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        moment = moment or now()
        if mode == "check":
            repository.source.check()
            Destination(cfg).preflight()
            return
        slot = capture_slot(mode, moment)
        if slot:
            day = moment.date().isoformat()
            errors = repository.state.setdefault("capture_errors", {}).setdefault(day, {})
            try:
                with Path(cfg["deploy_lock"]).open("a+") as deploy_lock:
                    fcntl.flock(deploy_lock, fcntl.LOCK_EX)
                    repository.snapshot(moment, slot)
                errors.pop(slot, None)
                if not errors:
                    repository.state["capture_errors"].pop(day, None)
            except (BackupError, OSError, subprocess.SubprocessError) as exc:
                errors[slot] = {"message": str(exc) if isinstance(exc, BackupError) else type(exc).__name__}
                raise
            finally:
                save_json(repository.state_path, repository.state)
        publish_pending(cfg, repository, moment.date().isoformat())
        print("Backup operation completed: " + mode)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["page", "hourly", "dump", "retry", "check", "bootstrap"])
    parser.add_argument("--config", default=str(Path(__file__).with_name("config.json")))
    args = parser.parse_args()
    os.umask(0o077)
    try:
        cfg = read_json(Path(args.config))
        run(cfg, args.mode)
    except (BackupError, OSError, ValueError, KeyError, subprocess.SubprocessError) as exc:
        print("Backup failed: " + (str(exc) if isinstance(exc, BackupError) else type(exc).__name__), file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
