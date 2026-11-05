#!/usr/bin/env python3
"""Read-only MySQL dump and resumable Notion attachment; Python stdlib only.

Does not start/stop containers, restore SQL, change source DB data, or delete
backups. SQL and API credentials must never be printed to stdout/journald.
"""
import datetime as dt
import gzip
import hashlib
import http.client
import json
import math
import os
from pathlib import Path
import re
import signal
import stat
import subprocess
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid
from zoneinfo import ZoneInfo

KST = ZoneInfo("Asia/Seoul")
MIB = 1024 * 1024
PART_BYTES = 10 * MIB
API_VERSION = "2026-03-11"


class BackupError(Exception):
    pass


def now():
    return dt.datetime.now(KST)


def secure_directory(path):
    path.mkdir(parents=True, exist_ok=True, mode=0o700)
    if path.is_symlink() or path.stat().st_uid != os.geteuid():
        raise BackupError("Backup directory must be owned by the service user and not a symlink")
    path.chmod(0o700)


def save_json(path, value):
    fd, name = tempfile.mkstemp(prefix=".metadata-", dir=path.parent)
    try:
        with os.fdopen(fd, "w") as out:
            json.dump(value, out, ensure_ascii=False, indent=2)
            out.write("\n")
            out.flush()
            os.fsync(out.fileno())
        os.replace(name, path)
    finally:
        Path(name).unlink(missing_ok=True)


def fingerprint(path):
    digest = hashlib.sha256()
    with path.open("rb") as src:
        for chunk in iter(lambda: src.read(MIB), b""):
            digest.update(chunk)
    total = 0
    tail = b""
    try:
        with gzip.open(path, "rb") as src:
            for chunk in iter(lambda: src.read(MIB), b""):
                total += len(chunk)
                tail = (tail + chunk)[-8192:]
    except (OSError, EOFError) as exc:
        raise BackupError("Compressed backup failed its integrity check") from exc
    if total == 0 or b"-- Dump completed on " not in tail:
        raise BackupError("Backup lacks the mysqldump completion marker")
    return {"sha256": digest.hexdigest(), "bytes": path.stat().st_size,
            "uncompressed_bytes": total}


class MySQL:
    def __init__(self, cfg):
        self.cfg = cfg
        for key in ("container", "database"):
            if not re.fullmatch(r"[A-Za-z0-9_][A-Za-z0-9_.-]*", cfg[key]):
                raise BackupError("Invalid container or database identifier")

    def command(self, program, args):
        # Existing container credentials remain inside that container. Password
        # values are neither interpolated into argv nor copied onto the host.
        return ["docker", "exec", self.cfg["container"], "sh", "-eu", "-c",
                ': "${MYSQL_ROOT_PASSWORD:?missing root credential}"; '
                'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec "$@"',
                "backup-client", program, "--user=root", *args]

    def check(self):
        query = ("SELECT COALESCE(ENGINE,'VIEW'),"
                 "COUNT(*),COALESCE(SUM(DATA_LENGTH+INDEX_LENGTH),0) "
                 "FROM information_schema.TABLES WHERE TABLE_SCHEMA='" +
                 self.cfg["database"] + "' GROUP BY ENGINE")
        cmd = self.command("mysql", ["--batch", "--skip-column-names", "--execute", query])
        try:
            result = subprocess.run(cmd, capture_output=True, timeout=30, check=True)
        except (subprocess.SubprocessError, OSError) as exc:
            raise BackupError("Cannot read MySQL metadata; container/authentication check required") from exc
        size, tables = 0, 0
        for line in result.stdout.decode().splitlines():
            engine, count, nbytes = line.split("\t")
            if engine not in ("InnoDB", "VIEW"):
                raise BackupError("Non-InnoDB table found; consistent online backup requires review")
            tables += int(count)
            size += int(nbytes)
        if not tables:
            raise BackupError("Source database contains no tables or does not exist")
        return {"table_count": tables, "estimated_bytes": size}

    def dump(self, path):
        cmd = self.command("mysqldump", [
            "--single-transaction", "--quick", "--no-tablespaces", "--source-data=2",
            "--set-gtid-purged=OFF", "--column-statistics=0",
            "--default-character-set=utf8mb4", "--hex-blob",
            "--routines", "--events", "--triggers", "--comments", "--dump-date",
            "--databases", self.cfg["database"]])
        # stderr is deliberately not logged: DB errors can contain sensitive
        # object names or content. The exit status is enough to fail closed.
        with tempfile.TemporaryFile() as errors:
            process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=errors,
                                       start_new_session=True)
            deadline = time.monotonic() + self.cfg.get("dump_timeout_seconds", 900)
            try:
                # The systemd unit also bounds total runtime. select prevents
                # an unresponsive database from blocking the pipe indefinitely.
                import select
                with path.open("xb") as raw:
                    with gzip.GzipFile(filename="", mode="wb", compresslevel=1,
                                       fileobj=raw, mtime=0) as zipped:
                        while True:
                            if time.monotonic() > deadline:
                                raise BackupError("MySQL dump timed out")
                            ready, _, _ = select.select([process.stdout], [], [], 1)
                            if ready:
                                chunk = os.read(process.stdout.fileno(), 64 * 1024)
                                if not chunk:
                                    break
                                zipped.write(chunk)
                        if process.wait(timeout=max(1, deadline - time.monotonic())) != 0:
                            raise BackupError("mysqldump failed; output is not a valid backup")
                    raw.flush()
                    os.fsync(raw.fileno())
            finally:
                if process.poll() is None:
                    os.killpg(process.pid, signal.SIGKILL)
                    process.wait()
                process.stdout.close()


class Notion:
    def __init__(self, cfg):
        token_file = Path(cfg["token_file"])
        info = token_file.lstat()
        if (not stat.S_ISREG(info.st_mode) or info.st_uid != os.geteuid()
                or info.st_mode & 0o077):
            raise BackupError("Notion token must be in a user-owned regular file with mode 600")
        self.token = token_file.read_text().strip()
        if not self.token or "\n" in self.token:
            raise BackupError("Notion token file is empty or invalid")
        self.cfg = cfg
        self.last_request_at = 0.0

    def headers(self):
        return {"Authorization": "Bearer " + self.token,
                "Notion-Version": API_VERSION, "Content-Type": "application/json"}

    def request(self, method, endpoint, payload=None):
        delay = 0.4 - (time.monotonic() - self.last_request_at)
        if delay > 0:
            time.sleep(delay)
        self.last_request_at = time.monotonic()
        body = json.dumps(payload).encode() if payload is not None else None
        request = urllib.request.Request("https://api.notion.com/v1" + endpoint,
                                         data=body, headers=self.headers(), method=method)
        # Mutations are intentionally not blindly retried. Their outcome can be
        # unknown after a lost response; the next run reconciles remote state.
        try:
            with urllib.request.urlopen(request, timeout=90) as response:
                return json.load(response)
        except urllib.error.HTTPError as exc:
            raise BackupError("Notion request failed (HTTP %d); verify access/limits" % exc.code) from None
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            raise BackupError("Notion request did not complete; remote outcome will be reconciled") from exc

    def preflight(self):
        source = self.request("GET", "/data_sources/" + self.cfg["data_source_id"])
        prop = source.get("properties", {}).get(self.cfg["title_property"], {})
        if prop.get("type") != "title":
            raise BackupError("Notion data source/title property does not match configuration")
        bot = self.request("GET", "/users/me").get("bot", {})
        return int(bot.get("workspace_limits", {}).get("max_file_upload_size_in_bytes", 5 * MIB))

    def find_page(self, title):
        result = self.request("POST", "/data_sources/" + self.cfg["data_source_id"] + "/query",
                              {"filter": {"property": self.cfg["title_property"],
                                          "title": {"equals": title}}, "page_size": 100})
        pages = result.get("results", [])
        if len(pages) > 1 or result.get("has_more"):
            raise BackupError("Multiple backup pages have the same date; manual review required")
        if pages:
            return pages[0]["id"]
        page = self.request("POST", "/pages", {
            "parent": {"type": "data_source_id", "data_source_id": self.cfg["data_source_id"]},
            "properties": {self.cfg["title_property"]: {
                "title": [{"type": "text", "text": {"content": title}}]}}})
        return page["id"]

    def blocks(self, page_id):
        result, cursor = [], None
        while True:
            suffix = "?page_size=100"
            if cursor:
                suffix += "&start_cursor=" + urllib.parse.quote(cursor)
            page = self.request("GET", "/blocks/" + page_id + "/children" + suffix)
            result.extend(page.get("results", []))
            cursor = page.get("next_cursor")
            if not page.get("has_more"):
                return result

    def send_part(self, upload_id, path, offset, length, part=None):
        boundary = "gujeuk-" + uuid.uuid4().hex
        prefix = b""
        if part is not None:
            prefix += ("--%s\r\nContent-Disposition: form-data; name=\"part_number\"\r\n\r\n%d\r\n"
                       % (boundary, part)).encode()
        prefix += ("--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n"
                   "Content-Type: application/gzip\r\n\r\n" % (boundary, path.name)).encode()
        ending = ("\r\n--%s--\r\n" % boundary).encode()
        connection = http.client.HTTPSConnection("api.notion.com", timeout=120)
        try:
            connection.putrequest("POST", "/v1/file_uploads/" + upload_id + "/send")
            for key, value in self.headers().items():
                if key != "Content-Type":
                    connection.putheader(key, value)
            connection.putheader("Content-Type", "multipart/form-data; boundary=" + boundary)
            connection.putheader("Content-Length", str(len(prefix) + length + len(ending)))
            connection.endheaders()
            connection.send(prefix)
            with path.open("rb") as src:
                src.seek(offset)
                remaining = length
                while remaining:
                    chunk = src.read(min(MIB, remaining))
                    if not chunk:
                        raise BackupError("Backup file changed during upload")
                    connection.send(chunk)
                    remaining -= len(chunk)
            connection.send(ending)
            response = connection.getresponse()
            data = response.read()
            if not 200 <= response.status < 300:
                raise BackupError("Notion file transfer failed (HTTP %d)" % response.status)
            return json.loads(data)
        except (OSError, http.client.HTTPException) as exc:
            raise BackupError("Notion file transfer interrupted; original backup is preserved") from exc
        finally:
            connection.close()

    def upload(self, path, meta, save, limit):
        if meta["bytes"] > limit:
            raise BackupError("Compressed dump exceeds this Notion workspace's file upload limit")
        upload_id = meta.get("upload_id")
        if upload_id:
            existing = self.request("GET", "/file_uploads/" + upload_id)
            if existing.get("status") == "uploaded":
                expiry = existing.get("expiry_time")
                if not expiry or dt.datetime.fromisoformat(expiry.replace("Z", "+00:00")) > now():
                    return upload_id
            elif existing.get("status") == "pending":
                expiry = existing.get("expiry_time")
                if not expiry or dt.datetime.fromisoformat(expiry.replace("Z", "+00:00")) > now():
                    raise BackupError("Previous file upload is pending; retry after its expiry")
        multipart = meta["bytes"] > 20 * MIB
        payload = {"filename": path.name, "content_type": "application/gzip"}
        if multipart:
            payload.update(mode="multi_part", number_of_parts=math.ceil(meta["bytes"] / PART_BYTES))
        created = self.request("POST", "/file_uploads", payload)
        meta["upload_id"] = created["id"]
        save()
        if multipart:
            for i, offset in enumerate(range(0, meta["bytes"], PART_BYTES), 1):
                self.send_part(created["id"], path, offset,
                               min(PART_BYTES, meta["bytes"] - offset), i)
            result = self.request("POST", "/file_uploads/" + created["id"] + "/complete", {})
        else:
            result = self.send_part(created["id"], path, 0, meta["bytes"])
        if result.get("status") != "uploaded":
            raise BackupError("Notion has not confirmed a completed file upload")
        return created["id"]

