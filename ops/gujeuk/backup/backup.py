#!/usr/bin/env python3
"""EC2-owned MySQL snapshots, continuous binlog archives and Notion publication."""
import argparse
import fcntl
import gzip
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

from backup_core import (BackupError, KST, MIB, MySQL, Notion, fingerprint,
                         now, save_json, secure_directory)


def read_json(path, default=None):
    return json.loads(path.read_text()) if path.exists() else default


def rich(text):
    return [{"type": "text", "text": {"content": text}}]


def paragraph(text):
    return {"object": "block", "type": "paragraph", "paragraph": {"rich_text": rich(text)}}


def archive_fingerprint(path, kind):
    if kind == "dump":
        return fingerprint(path)
    digest, size, head = hashlib.sha256(), 0, b""
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(MIB), b""):
            digest.update(chunk)
    with gzip.open(path, "rb") as stream:
        for chunk in iter(lambda: stream.read(MIB), b""):
            head = (head + chunk)[:4]
            size += len(chunk)
    if head != b"\xfebin" or size < 4:
        raise BackupError("Invalid binary log archive")
    return {"sha256": digest.hexdigest(), "bytes": path.stat().st_size,
            "uncompressed_bytes": size}


class Source(MySQL):
    def query(self, sql):
        try:
            value = subprocess.run(self.command("mysql", ["--batch", "--skip-column-names",
                                   "--execute", sql]), capture_output=True, check=True, timeout=30)
            return [line.split("\t") for line in value.stdout.decode().splitlines()]
        except (subprocess.SubprocessError, OSError):
            raise BackupError("MySQL metadata command failed") from None

    def identity(self):
        values = self.query("SELECT @@server_uuid, @@log_bin, @@binlog_format, "
                            "@@log_bin_basename, @@sync_binlog, @@innodb_flush_log_at_trx_commit")
        uuid, enabled, fmt, basename, sync, flush = values[0]
        if enabled != "1" or fmt != "ROW":
            raise BackupError("Binary logging must already be enabled in ROW format")
        if sync != "1" or flush != "1":
            raise BackupError("Durability requires sync_binlog=1 and innodb_flush_log_at_trx_commit=1")
        if not re.fullmatch(r"/[A-Za-z0-9_./-]+", basename) or ".." in Path(basename).parts:
            raise BackupError("Unexpected binlog storage path")
        return {"server_uuid": uuid, "basename": basename}

    def position(self):
        row = self.query("SHOW MASTER STATUS")[0]
        return {"file": row[0], "position": int(row[1])}

    def logs(self):
        result = []
        for row in self.query("SHOW BINARY LOGS"):
            if not re.fullmatch(r"[A-Za-z0-9_.-]+\.[0-9]+", row[0]):
                raise BackupError("Unexpected binary log filename")
            if len(row) > 2 and row[2] != "No":
                raise BackupError("Encrypted source binlogs require a different collection method")
            result.append((row[0], int(row[1])))
        return result

    def rotate(self):
        self.query("FLUSH BINARY LOGS")

    def archive_log(self, log_name, expected_size, partial, identity):
        source_path = str(Path(identity["basename"]).parent / log_name)
        try:
            subprocess.run(["docker", "run", "--rm", "--platform", "linux/amd64", "--network", "none",
                            "--volumes-from", self.cfg["container"]+":ro", "--entrypoint", "mysqlbinlog",
                            self.cfg.get("mysqlbinlog_image", "mysql:8.0-debian"), "--no-defaults",
                            "--verify-binlog-checksum", source_path],
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                           check=True, timeout=180)
            with partial.open("xb") as raw:
                with gzip.GzipFile(filename="", mode="wb", fileobj=raw, mtime=0) as zipped:
                    # Copy only a closed, checksum-validated log. stderr never reaches journald.
                    with tempfile.TemporaryFile() as errors:
                        process = subprocess.Popen(["docker", "exec", self.cfg["container"],
                                                    "cat", source_path], stdout=subprocess.PIPE,
                                                   stderr=errors)
                        try:
                            shutil.copyfileobj(process.stdout, zipped, 64 * 1024)
                            if process.wait(timeout=180):
                                raise BackupError("Cannot copy closed binary log")
                        finally:
                            process.stdout.close()
                            if process.poll() is None:
                                process.kill()
                                process.wait()
                raw.flush()
                os.fsync(raw.fileno())
            checked = archive_fingerprint(partial, "binlog")
            if checked["uncompressed_bytes"] != expected_size:
                raise BackupError("Binary log size changed or copy is incomplete")
            return checked
        except (subprocess.SubprocessError, OSError):
            raise BackupError("Binary log checksum or copy validation failed") from None


def dump_position(path):
    pattern = re.compile(rb"(?:SOURCE|MASTER)_LOG_FILE='([^']+)', "
                         rb"(?:SOURCE|MASTER)_LOG_POS=([0-9]+)")
    with gzip.open(path, "rb") as stream:
        # Coordinates are near the start; never decode/log the dump's data rows.
        found = pattern.search(stream.read(2 * MIB))
    if not found:
        raise BackupError("Dump has no consistent binlog coordinates")
    return {"file": found[1].decode("ascii"), "position": int(found[2])}


class Repository:
    def __init__(self, cfg, source=None):
        self.cfg = cfg
        self.root = Path(cfg["backup_directory"])
        secure_directory(self.root)
        self.source = source or Source(cfg)
        self.state_path = self.root / "state.json"
        self.state = read_json(self.state_path, {})

    def directory(self, day):
        if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", day):
            raise BackupError("Invalid backup date")
        path = self.root / day
        secure_directory(path)
        return path

    def check_source(self):
        identity = self.source.identity()
        if self.state.get("server_uuid", identity["server_uuid"]) != identity["server_uuid"]:
            raise BackupError("MySQL identity changed; establish a new baseline explicitly")
        return identity

    def space(self, required):
        if shutil.disk_usage(self.root).free < max(self.cfg.get("minimum_free_bytes", 512*MIB), required):
            raise BackupError("Insufficient disk space; existing backups were preserved")

    def snapshot(self, moment, baseline=False):
        identity = self.check_source()
        day = moment.date().isoformat()
        directory = self.directory(day)
        kind = "baseline" if baseline else "dump"
        manifest = directory / (kind + ".json")
        if manifest.exists():
            meta = read_json(manifest)
            checked = archive_fingerprint(directory / meta["filename"], "dump")
            if checked["sha256"] != meta["sha256"]:
                raise BackupError("Existing dump checksum mismatch")
            if not self.state.get("cursor"):
                if meta["server_uuid"] != identity["server_uuid"]:
                    raise BackupError("Baseline identity mismatch")
                self.state.update(server_uuid=identity["server_uuid"], cursor=meta["position"],
                                  baseline=str(manifest.relative_to(self.root)))
                save_json(self.state_path, self.state)
            return manifest
        stats = self.source.check()
        self.space(2 * stats["estimated_bytes"])
        name = "gujeuk-prod_%s_%s_%s.sql.gz" % (day, moment.strftime("%H%M%S"), kind)
        final, partial = directory / name, directory / (name + ".partial")
        try:
            self.source.dump(partial)
            checked = archive_fingerprint(partial, "dump")
            position = dump_position(partial)
            os.replace(partial, final)
            meta = {"kind": "dump", "baseline": baseline, "date": day, "filename": name,
                    "started_at": moment.isoformat(), "completed_at": now().isoformat(),
                    "database": self.cfg["database"], "position": position,
                    "source_metadata": stats, **identity, **checked}
            save_json(manifest, meta)
            if not self.state.get("cursor"):
                self.state.update(server_uuid=identity["server_uuid"], cursor=position,
                                  baseline=str(manifest.relative_to(self.root)))
                save_json(self.state_path, self.state)
            return manifest
        finally:
            partial.unlink(missing_ok=True)

    def collect(self, moment):
        if not self.cfg.get("allow_instance_binlogs", False):
            raise BackupError("Confirm instance-wide binlog scope before collecting")
        identity = self.check_source()
        cursor = self.state.get("cursor")
        if not cursor:
            raise BackupError("Create and verify the initial baseline dump first")
        self.source.rotate()
        active = self.source.position()["file"]
        logs = self.source.logs()
        names = [name for name, _ in logs]
        if cursor["file"] not in names:
            raise BackupError("Required source binlog was purged; refusing to skip the gap")
        start, stop = names.index(cursor["file"]), names.index(active)
        if start > stop:
            raise BackupError("Binary log sequence went backwards")
        directory = self.directory(moment.date().isoformat())
        completed = []
        for name, size in logs[start:stop]:
            if name == cursor["file"] and cursor["position"] > size:
                raise BackupError("Backup start position exceeds the closed binary log size")
            self.space(2*size)
            archive_name = "%s_%s.binlog.gz" % (identity["server_uuid"], name)
            manifest = directory / (name + ".json")
            final, partial = directory / archive_name, directory / (archive_name + ".partial")
            if manifest.exists():
                meta = read_json(manifest)
                if archive_fingerprint(final, "binlog")["sha256"] != meta["sha256"]:
                    raise BackupError("Existing binary log archive checksum mismatch")
            else:
                try:
                    checked = self.source.archive_log(name, size, partial, identity)
                    os.replace(partial, final)
                    meta = {"kind": "binlog", "date": moment.date().isoformat(),
                            "filename": archive_name, "started_at": moment.isoformat(),
                            "completed_at": now().isoformat(), "log_file": name,
                            "start_position": cursor["position"] if name == cursor["file"] else 4,
                            "end_position": size, **identity, **checked}
                    save_json(manifest, meta)
                finally:
                    partial.unlink(missing_ok=True)
            # Commit each file before advancing. Crashes replay the same immutable archive.
            completed.append(manifest)
            next_name = names[names.index(name)+1]
            self.state["cursor"] = {"file": next_name, "position": 4}
            save_json(self.state_path, self.state)
        return completed

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
        directory = manifest.parent
        path = directory / meta["filename"]
        if path.parent != directory or path.is_symlink():
            raise BackupError("Invalid archive path")
        checked = archive_fingerprint(path, meta["kind"])
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
                        "bytes", "sha256", "server_uuid")}
            for key in ("position", "log_file", "start_position", "end_position", "baseline"):
                if key in meta:
                    details[key] = meta[key]
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
        logs = [m for m in items if m["kind"] == "binlog" and m.get("notion_attached")]
        dumps = [m for m in items if m["kind"] == "dump" and not m.get("baseline") and m.get("notion_attached")]
        all_uploaded = bool(items) and all(m.get("notion_attached") for m in items)
        props = {"binlog 파일 수": {"number": len(logs)},
                 "상태": {"select": {"name": "완료" if dumps and all_uploaded else "진행 중"}},
                 "검증": {"select": {"name": "복원 확인" if all_uploaded and any(
                     m.get("restore_verified") for m in items) else
                     "무결성 확인" if all_uploaded else "미검증"}}}
        if logs:
            props["마지막 binlog 시각"] = {"date": {"start": max(m["started_at"] for m in logs)}}
        if dumps:
            props["덤프 시각"] = {"date": {"start": max(m["started_at"] for m in dumps)}}
        if repository.state.get("capture_errors", {}).get(day):
            props["상태"] = {"select": {"name": "오류"}}
            props["오류 내용"] = {"rich_text": rich("Source capture failed; check systemd journal and retry the capture.")}
        else:
            props["오류 내용"] = {"rich_text": []}
        # Reconcile FILES properties from verified body attachments, including retries.
        for kind, prop in (("binlog", "binlog 파일"), ("dump", "덤프 파일")):
            files = []
            for m in items:
                if m["kind"] == kind and m.get("notion_attached"):
                    files.append({"type": "file_upload", "name": m["filename"],
                                  "file_upload": {"id": m["upload_id"]}})
            if files and len(files) <= 100:
                props[prop] = {"files": files}
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
    moment = moment or now()
    if moment.tzinfo is None:
        raise BackupError("Timezone-aware schedule required")
    moment = moment.astimezone(KST)
    repository = Repository(cfg)
    with (repository.root / ".lock").open("a+") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        if mode == "check":
            repository.check_source()
            repository.source.check()
            Destination(cfg).preflight()
            print("Source and Notion checks passed; timers are not changed")
            return
        if mode == "verify":
            for manifest in repository.manifests():
                meta = read_json(manifest)
                checked = archive_fingerprint(manifest.parent / meta["filename"], meta["kind"])
                if any(meta.get(k) != v for k, v in checked.items()):
                    raise BackupError("Local archive validation failed")
            print("Local archive checks passed; this is not a restore test")
            return
        if mode in ("bootstrap", "dump", "binlog"):
            day = moment.date().isoformat()
            errors = repository.state.setdefault("capture_errors", {})
            try:
                if not repository.state.get("cursor"):
                    if mode != "bootstrap":
                        raise BackupError("Initial baseline is missing; run bootstrap first")
                    repository.snapshot(moment, baseline=True)
                if mode in ("binlog", "dump"):
                    repository.collect(moment)
                if mode == "dump":
                    repository.snapshot(moment)
                    repository.collect(now())
                errors.pop(day, None)
                save_json(repository.state_path, repository.state)
            except BackupError:
                errors[day] = {"mode": mode, "at": now().isoformat()}
                save_json(repository.state_path, repository.state)
                raise
        # Source capture precedes network access. Notion outages cannot block local backup.
        publish_pending(cfg, repository, moment.date().isoformat())
        print("Backup operation completed: " + mode)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["page", "binlog", "dump", "retry", "check", "verify", "bootstrap"])
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
