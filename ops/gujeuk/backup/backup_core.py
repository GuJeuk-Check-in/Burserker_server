"""MySQL single-database dumps and Notion API access."""
import datetime as dt
import gzip
import hashlib
import json
import os
from pathlib import Path
import shutil
import ssl
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
API_VERSION = "2026-03-11"


class BackupError(Exception):
    pass


def now():
    return dt.datetime.now(KST)


def secure_directory(path):
    path.mkdir(parents=True, exist_ok=True, mode=0o700)
    path.chmod(0o700)


def save_json(path, value):
    temporary = path.with_suffix(".tmp")
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n")
    temporary.replace(path)


def fingerprint(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(MIB), b""):
            digest.update(chunk)
    with gzip.open(path, "rb") as stream:
        total, tail = 0, b""
        for chunk in iter(lambda: stream.read(MIB), b""):
            total += len(chunk)
            tail = (tail + chunk)[-8192:]
    if not total or b"-- Dump completed on " not in tail:
        raise BackupError("MySQL dump did not complete")
    return {"sha256": digest.hexdigest(), "bytes": path.stat().st_size, "uncompressed_bytes": total}


class MySQL:
    def __init__(self, cfg):
        self.cfg = cfg

    def command(self, program, args):
        return ["docker", "exec", self.cfg["container"], "sh", "-eu", "-c",
                'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec "$@"',
                "backup-client", program, "--user=root", *args]

    def check(self):
        query = ("SELECT ENGINE,COUNT(*),COALESCE(SUM(DATA_LENGTH+INDEX_LENGTH),0) "
                 "FROM information_schema.TABLES WHERE TABLE_SCHEMA='" +
                 self.cfg["database"] + "' GROUP BY ENGINE")
        result = subprocess.run(self.command("mysql", ["-Nse", query]),
                                stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, timeout=30)
        if result.returncode:
            raise BackupError("Cannot read MySQL metadata")
        tables, size = 0, 0
        for line in result.stdout.decode().splitlines():
            engine, count, nbytes = line.split("\t")
            if engine not in ("InnoDB", "NULL"):
                raise BackupError("Online backup requires InnoDB tables")
            tables += int(count)
            size += int(nbytes)
        if not tables:
            raise BackupError("Source database has no tables")
        return {"table_count": tables, "estimated_bytes": size}

    def dump(self, path):
        args = ["--single-transaction", "--quick", "--no-tablespaces",
                "--set-gtid-purged=OFF", "--column-statistics=0", "--hex-blob",
                "--default-character-set=utf8mb4", "--routines", "--events", "--triggers",
                "--comments", "--dump-date", "--databases", self.cfg["database"]]
        with tempfile.TemporaryFile(dir=path.parent) as sql:
            result = subprocess.run(self.command("mysqldump", args), stdout=sql,
                                    stderr=subprocess.DEVNULL, timeout=900)
            if result.returncode:
                raise BackupError("MySQL dump failed")
            sql.seek(0)
            with gzip.open(path, "wb", compresslevel=1) as archive:
                shutil.copyfileobj(sql, archive)


class Notion:
    def __init__(self, cfg):
        self.cfg = cfg
        self.token = Path(cfg["token_file"]).read_text().strip()
        self.last_request_at = 0

    def request(self, method, endpoint, payload=None, content_type="application/json"):
        time.sleep(max(0, 0.4 - (time.monotonic() - self.last_request_at)))
        self.last_request_at = time.monotonic()
        body = payload if isinstance(payload, bytes) else (
            json.dumps(payload).encode() if payload is not None else None)
        request = urllib.request.Request("https://api.notion.com/v1" + endpoint, data=body,
            headers={"Authorization": "Bearer " + self.token, "Notion-Version": API_VERSION,
                     "Content-Type": content_type}, method=method)
        try:
            with urllib.request.urlopen(request, timeout=90) as response:
                return json.load(response)
        except urllib.error.HTTPError as exc:
            raise BackupError("Notion request failed (HTTP %d)" % exc.code) from None
        except (urllib.error.URLError, TimeoutError, OSError) as exc:
            if isinstance(getattr(exc, "reason", exc), ssl.SSLCertVerificationError):
                raise BackupError("Set SSL_CERT_FILE to Python's trusted CA bundle") from None
            raise BackupError("Notion connection failed; local files remain available") from None

    def preflight(self):
        source = self.request("GET", "/data_sources/" + self.cfg["data_source_id"])
        if source["properties"][self.cfg["title_property"]]["type"] != "title":
            raise BackupError("Notion title property does not match configuration")
        bot = self.request("GET", "/users/me")["bot"]
        return min(20 * MIB, bot.get("workspace_limits", {}).get("max_file_upload_size_in_bytes", 5*MIB))

    def resolve_data_source(self, database_id):
        sources = self.request("GET", "/databases/" + str(uuid.UUID(database_id)))["data_sources"]
        if len(sources) != 1:
            raise BackupError("Specify the data source ID for a database with multiple sources")
        return sources[0]["id"]

    def find_page(self, title, children, properties):
        title_property = self.cfg["title_property"]
        pages = self.request("POST", "/data_sources/" + self.cfg["data_source_id"] + "/query",
            {"filter": {"property": title_property, "title": {"equals": title}}})["results"]
        if pages:
            return pages[0]["id"]
        properties[title_property] = {"title": [{"text": {"content": title}}]}
        return self.request("POST", "/pages", {
            "parent": {"type": "data_source_id", "data_source_id": self.cfg["data_source_id"]},
            "properties": properties, "children": children})["id"]

    def blocks(self, page_id):
        blocks, cursor = [], None
        while True:
            query = "?page_size=100" + ("&start_cursor=" + urllib.parse.quote(cursor) if cursor else "")
            page = self.request("GET", "/blocks/" + page_id + "/children" + query)
            blocks.extend(page["results"])
            if not page.get("has_more"):
                return blocks
            cursor = page["next_cursor"]

    def upload(self, path, meta, save, limit):
        if path.stat().st_size > limit:
            raise BackupError("Dump exceeds the Notion file upload limit")
        if meta.get("upload_id"):
            existing = self.request("GET", "/file_uploads/" + meta["upload_id"])
            if existing["status"] == "uploaded":
                return existing["id"]
            if existing["status"] != "pending":
                meta.pop("upload_id")
        if not meta.get("upload_id"):
            meta["upload_id"] = self.request("POST", "/file_uploads", {
                "filename": path.name, "content_type": "application/gzip"})["id"]
            save()
        boundary = "gujeuk-" + uuid.uuid4().hex
        body = ("--%s\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%s\"\r\n"
                "Content-Type: application/gzip\r\n\r\n" % (boundary, path.name)).encode()
        body += path.read_bytes() + ("\r\n--%s--\r\n" % boundary).encode()
        result = self.request("POST", "/file_uploads/" + meta["upload_id"] + "/send", body,
                              "multipart/form-data; boundary=" + boundary)
        if result["status"] != "uploaded":
            raise BackupError("Notion did not confirm the upload")
        return meta["upload_id"]
