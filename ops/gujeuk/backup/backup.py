#!/usr/bin/env python3
"""EC2-owned full snapshots of a single MySQL database and Notion publication."""
import argparse
import fcntl
import datetime as dt
import json
import os
from pathlib import Path
import re
import shutil
import sys
import uuid

from backup_core import (BackupError, KST, MIB, MySQL, Notion, fingerprint,
                         now, save_json, secure_directory)


def read_json(path, default=None):
    return json.loads(path.read_text()) if path.exists() else default


def rich(text):
    return [{"type": "text", "text": {"content": text}}]


def paragraph(text):
    return {"object": "block", "type": "paragraph", "paragraph": {"rich_text": rich(text)}}


def capture_slot(mode, moment):
    if mode == "bootstrap":
        return "bootstrap"
    if mode == "hourly" and 1 <= moment.hour <= 19 and (moment.hour, moment.minute) < (19, 30):
        return "hourly-%02d" % moment.hour
    if mode == "dump" and (moment.hour, moment.minute) >= (19, 30):
        return "final"
    return None


class Repository:
    def __init__(self, cfg, source=None):
        self.cfg = cfg
        self.root = Path(cfg["backup_directory"])
        secure_directory(self.root)
        self.source = source or MySQL(cfg)
        self.state_path = self.root / "state.json"
        self.state = read_json(self.state_path, {})

    def directory(self, day):
        if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", day):
            raise BackupError("Invalid backup date")
        path = self.root / day
        secure_directory(path)
        return path

    def archive(self, manifest, meta):
        name = meta["filename"]
        path = manifest.parent / name
        if Path(name).name != name or path.is_symlink():
            raise BackupError("Invalid archive path")
        return path

    def snapshot(self, moment, slot):
        if not re.fullmatch(r"hourly-(?:0[1-9]|1[0-9])|final|bootstrap", slot):
            raise BackupError("Invalid snapshot slot")
        day = moment.date().isoformat()
        directory = self.directory(day)
        manifest = directory / (slot + ".json")
        if manifest.exists():
            meta = read_json(manifest)
            checked = fingerprint(self.archive(manifest, meta))
            if any(meta.get(key) != value for key, value in checked.items()):
                raise BackupError("Existing dump checksum mismatch")
            return manifest
        stats = self.source.check()
        if shutil.disk_usage(self.root).free < max(
                self.cfg.get("minimum_free_bytes", 512*MIB), 2*stats["estimated_bytes"]):
            raise BackupError("Insufficient disk space; existing backups were preserved")
        name = "gujeuk-prod_%s_%s_%s_%s.sql.gz" % (
            day, moment.strftime("%H%M%S"), slot, uuid.uuid4().hex[:8])
        final, partial = directory / name, directory / (name + ".partial")
        try:
            self.source.dump(partial)
            checked = fingerprint(partial)
            os.replace(partial, final)
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
        page_id = self.find_page(day + " 구즉 prod")
        current = self.request("GET", "/pages/" + page_id)
        props = current.get("properties", {})
        changes = {"백업일": {"date": {"start": day}}, "환경": {"select": {"name": "prod"}}}
        if not props.get("상태", {}).get("select"):
            changes["상태"] = {"select": {"name": "진행 중"}}
        self.request("PATCH", "/pages/"+page_id, {"properties": changes})
        return page_id

    def publish(self, repository, manifest, limit):
        meta = read_json(manifest)
        path = repository.archive(manifest, meta)
        checked = fingerprint(path)
        if any(meta.get(key) != value for key, value in checked.items()):
            raise BackupError("Archive integrity check failed before publication")
        if meta.get("notion_attached"):
            return
        page_id = self.page(meta["date"])
        marker = "gujeuk:%s:%s" % (meta["filename"], meta["sha256"])

        def attached():
            return any(block.get("type") == "file" and
                       "".join(t.get("plain_text", t.get("text", {}).get("content", ""))
                               for t in block["file"].get("caption", [])) == marker
                       for block in self.blocks(page_id))

        if not attached():
            upload_id = self.upload(path, meta, lambda: save_json(manifest, meta), limit)
            details = {k: meta[k] for k in ("kind", "filename", "started_at", "completed_at",
                        "bytes", "sha256", "database", "slot")}
            self.request("PATCH", "/blocks/"+page_id+"/children", {"children": [
                paragraph(json.dumps(details, ensure_ascii=False)),
                {"object": "block", "type": "file", "file": {"type": "file_upload",
                 "file_upload": {"id": upload_id}, "caption": rich(marker)}}]})
            if not attached():
                raise BackupError("File upload succeeded but attachment was not confirmed")
        meta.update(notion_attached=True, page_id=page_id, attached_at=now().isoformat(),
                    summary_synced=False)
        save_json(manifest, meta)

    def update_day(self, repository, day):
        items = [read_json(p) for p in repository.directory(day).glob("*.json")]
        uploaded = [m for m in items if m.get("notion_attached")]
        hourly = [m for m in uploaded if m["slot"].startswith("hourly-")]
        finals = [m for m in uploaded if m["slot"] == "final"]
        all_uploaded = bool(items) and len(uploaded) == len(items)
        expected = {"hourly-%02d" % hour for hour in range(1, 20)} | {"final"}
        missing = expected - {m["slot"] for m in uploaded}
        finished = day < now().date().isoformat() or (
            day == now().date().isoformat() and (now().hour, now().minute) >= (19, 30))
        errors = repository.state.get("capture_errors", {}).get(day, {})
        issue = bool(errors) or (finished and bool(missing))
        props = {"시간별 덤프 수": {"number": len(hourly)},
                 "상태": {"select": {"name": "오류" if issue else
                     "완료" if not missing and all_uploaded else "진행 중"}},
                 "검증": {"select": {"name": "복원 확인" if all_uploaded and all(
                     m.get("restore_verified") for m in items) else
                     "무결성 확인" if all_uploaded else "미검증"}},
                 "오류 내용": {"rich_text": rich("Capture/upload missing: " + ", ".join(sorted(missing)))
                     if issue else []}}
        if uploaded:
            props["마지막 백업 시각"] = {"date": {"start": max(m["started_at"] for m in uploaded)}}
        if finals:
            props["최종 덤프 시각"] = {"date": {"start": finals[0]["started_at"]}}
        for selected, prop in (([m for m in uploaded if m["slot"] != "final"], "시간별 덤프 파일"),
                               (finals, "최종 덤프 파일")):
            props[prop] = {"files": [{"type": "file_upload", "name": m["filename"],
                            "file_upload": {"id": m["upload_id"]}} for m in selected]}
        self.request("PATCH", "/pages/"+self.page(day), {"properties": props})
        for manifest in repository.directory(day).glob("*.json"):
            meta = read_json(manifest)
            if meta.get("notion_attached"):
                meta["summary_synced"] = True
                save_json(manifest, meta)


def publish_pending(cfg, repository, day):
    destination = Destination(cfg)
    limit = destination.preflight()
    destination.page(day)
    touched, failures = {day}, []
    previous_day = (dt.date.fromisoformat(day) - dt.timedelta(days=1)).isoformat()
    if (repository.root / previous_day).is_dir():
        touched.add(previous_day)
    touched.update(repository.state.get("capture_errors", {}))
    for manifest in repository.manifests():
        meta = read_json(manifest)
        if not meta.get("summary_synced"):
            touched.add(meta["date"])
        if not meta.get("notion_attached"):
            try:
                destination.publish(repository, manifest, limit)
            except BackupError:
                failures.append(manifest)
    # Refresh summaries even when the last run attached a file but lost its PATCH response.
    for date in sorted(touched):
        destination.update_day(repository, date)
    if failures:
        raise BackupError("Local archives are safe; one or more attachments await retry")


def run(cfg, mode, moment=None):
    repository = Repository(cfg)
    with (repository.root / ".lock").open("a+") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        moment = moment or now()
        if moment.tzinfo is None:
            raise BackupError("Timezone-aware schedule required")
        moment = moment.astimezone(KST)
        if mode == "check":
            repository.source.check()
            Destination(cfg).preflight()
            print("Source and Notion checks passed; timers are not changed")
            return
        if mode == "verify":
            for manifest in repository.manifests():
                meta = read_json(manifest)
                checked = fingerprint(repository.archive(manifest, meta))
                if any(meta.get(k) != v for k, v in checked.items()):
                    raise BackupError("Local archive validation failed")
            print("Local archive checks passed; this is not a restore test")
            return
        slot = capture_slot(mode, moment)
        if slot:
            day = moment.date().isoformat()
            errors = repository.state.setdefault("capture_errors", {}).setdefault(day, {})
            try:
                repository.snapshot(moment, slot)
                errors.pop(slot, None)
                if not errors:
                    repository.state["capture_errors"].pop(day, None)
                save_json(repository.state_path, repository.state)
            except BackupError:
                errors[slot] = {"at": now().isoformat()}
                save_json(repository.state_path, repository.state)
                raise
        # Source capture precedes network access. Notion outages cannot block local backup.
        publish_pending(cfg, repository, moment.date().isoformat())
        print("Backup operation completed: " + mode)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["page", "hourly", "dump", "retry", "check", "verify", "bootstrap"])
    parser.add_argument("--config", default=str(Path(__file__).with_name("config.json")))
    args = parser.parse_args()
    os.umask(0o077)
    try:
        cfg = read_json(Path(args.config))
        if not cfg:
            raise BackupError("Private config.json is missing")
        with Path(cfg["deploy_lock"]).open("a+") as lock:
            fcntl.flock(lock, fcntl.LOCK_EX)
            run(cfg, args.mode)
    except (BackupError, OSError, ValueError, KeyError, IndexError) as exc:
        message = str(exc) if isinstance(exc, BackupError) else type(exc).__name__
        print("Backup failed: " + message, file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
