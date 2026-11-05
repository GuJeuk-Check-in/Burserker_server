import datetime as dt
import gzip
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import backup

SQL = (b"-- CHANGE REPLICATION SOURCE TO SOURCE_LOG_FILE='binlog.000001', SOURCE_LOG_POS=100;\n"
       b"CREATE TABLE item(id INT);\n-- Dump completed on 2026-11-05\n")


class Source:
    def __init__(self):
        self.active = 1
        self.available = [("binlog.000001", 200)]
        self.copied = []
        self.fail = False
        self.uuid = "fixture-server"

    def identity(self):
        return {"server_uuid": self.uuid, "basename": "/var/lib/mysql/binlog"}

    def check(self):
        return {"estimated_bytes": 100, "table_count": 1}

    def dump(self, path):
        with gzip.open(path, "wb") as stream:
            stream.write(SQL)
        if self.fail:
            raise backup.BackupError("fixture dump failed")

    def rotate(self):
        self.active += 1
        self.available.append(("binlog.%06d" % self.active, 200))

    def position(self):
        return {"file": "binlog.%06d" % self.active, "position": 4}

    def logs(self):
        return self.available

    def archive_log(self, name, size, path, identity):
        self.copied.append(name)
        if self.fail:
            raise backup.BackupError("fixture copy failed")
        with gzip.open(path, "wb") as stream:
            stream.write(b"\xfebin" + b"x"*196)
        return backup.archive_fingerprint(path, "binlog")


class Backups(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.source = Source()
        self.cfg = {"container": "fixture", "database": "fixture",
                    "backup_directory": str(self.root), "minimum_free_bytes": 0,
                    "allow_instance_binlogs": True}
        self.repo = backup.Repository(self.cfg, self.source)
        self.at = dt.datetime(2026, 11, 5, 19, 30, tzinfo=backup.KST)

    def baseline(self):
        return self.repo.snapshot(self.at, baseline=True)

    def test_failed_dump_cannot_become_a_baseline(self):
        self.source.fail = True
        with self.assertRaises(backup.BackupError):
            self.baseline()
        self.assertNotIn("cursor", self.repo.state)
        self.assertFalse(list(self.root.rglob("*.json")))
        self.assertFalse(list(self.root.rglob("*.partial")))

    def test_baseline_recovers_crash_between_manifest_and_cursor_commit(self):
        self.baseline()
        self.repo.state = {}
        self.baseline()
        self.assertEqual(self.repo.state["cursor"]["position"], 100)

    def test_only_closed_logs_are_copied_and_not_repeated(self):
        self.baseline()
        self.repo.collect(self.at)
        self.assertEqual(self.source.copied, ["binlog.000001"])
        self.repo.collect(self.at)
        self.assertEqual(self.source.copied, ["binlog.000001", "binlog.000002"])

    def test_failed_copy_preserves_cursor_and_is_retried_without_gap(self):
        self.baseline()
        self.source.fail = True
        with self.assertRaises(backup.BackupError):
            self.repo.collect(self.at)
        self.assertEqual(self.repo.state["cursor"]["file"], "binlog.000001")
        self.source.fail = False
        self.repo.collect(self.at)
        self.assertEqual(self.source.copied[-2:], ["binlog.000001", "binlog.000002"])

    def test_source_purge_is_an_error_not_a_new_start(self):
        self.baseline()
        self.source.available = []
        with self.assertRaisesRegex(backup.BackupError, "purged"):
            self.repo.collect(self.at)
        self.assertEqual(self.repo.state["cursor"]["file"], "binlog.000001")

    def test_next_day_archive_includes_uncollected_overnight_log(self):
        self.baseline()
        self.repo.collect(self.at)
        tomorrow = self.at + dt.timedelta(hours=5, minutes=30)
        archived = self.repo.collect(tomorrow)
        self.assertEqual(archived[0].parent.name, "2026-11-06")
        self.assertEqual(json.loads(archived[0].read_text())["log_file"], "binlog.000002")

    def test_another_mysql_instance_cannot_continue_the_chain(self):
        self.baseline()
        self.source.uuid = "replacement-server"
        with self.assertRaisesRegex(backup.BackupError, "identity changed"):
            self.repo.collect(self.at)

    def test_instance_wide_scope_must_be_explicitly_accepted(self):
        self.baseline()
        self.cfg["allow_instance_binlogs"] = False
        with self.assertRaisesRegex(backup.BackupError, "scope"):
            self.repo.collect(self.at)
        self.assertFalse(self.source.copied)

    def test_corrupted_local_archive_blocks_retry(self):
        self.baseline()
        manifest = self.repo.collect(self.at)[0]
        meta = json.loads(manifest.read_text())
        path = manifest.parent / meta["filename"]
        path.write_bytes(b"not gzip")
        with self.assertRaises(OSError):
            backup.archive_fingerprint(path, "binlog")

    def test_notion_outage_does_not_prevent_local_dump(self):
        with patch.object(backup, "Source", return_value=self.source), \
             patch.object(backup, "Destination", side_effect=backup.BackupError("offline")):
            with self.assertRaises(backup.BackupError):
                backup.run(self.cfg, "bootstrap", self.at)
        self.assertEqual(len(list(self.root.rglob("*.sql.gz"))), 1)

    def test_retry_does_not_capture_or_rotate_source(self):
        with patch.object(backup, "Source", return_value=self.source), \
             patch.object(backup, "publish_pending"):
            backup.run(self.cfg, "retry", self.at)
        self.assertEqual(self.source.active, 1)
        self.assertFalse(list(self.root.rglob("*.sql.gz")))

    def test_legacy_mysql_master_coordinates_are_supported(self):
        file = self.root / "legacy.gz"
        with gzip.open(file, "wb") as out:
            out.write(SQL.replace(b"SOURCE_LOG_", b"MASTER_LOG_"))
        self.assertEqual(backup.dump_position(file)["position"], 100)

    def test_lost_attachment_response_is_reconciled_without_a_duplicate(self):
        class Destination(backup.Destination):
            def __init__(self):
                self.children = []
                self.appends = 0
                self.uploads = 0
                self.lose_response = True

            def page(self, day):
                return "fixture-page"

            def blocks(self, page_id):
                return self.children

            def upload(self, path, meta, save, limit):
                self.uploads += 1
                meta["upload_id"] = "fixture-upload"
                save()
                return "fixture-upload"

            def request(self, method, endpoint, payload):
                self.appends += 1
                self.children.extend(payload["children"])
                if self.lose_response:
                    raise backup.BackupError("Lost response after server accepted attachment")

        manifest = self.baseline()
        destination = Destination()
        with self.assertRaises(backup.BackupError):
            destination.publish(self.repo, manifest, 10000)
        destination.lose_response = False
        destination.publish(self.repo, manifest, 10000)
        self.assertEqual(destination.appends, 1)
        self.assertEqual(destination.uploads, 1)
        self.assertTrue(json.loads(manifest.read_text())["notion_attached"])

    def test_baseline_archive_tampering_is_rejected_before_reuse(self):
        manifest = self.baseline()
        meta = json.loads(manifest.read_text())
        with gzip.open(manifest.parent/meta["filename"], "wb") as stream:
            stream.write(SQL+b"unexpected data")
        with self.assertRaisesRegex(backup.BackupError, "checksum"):
            self.baseline()

    def test_private_token_client_uses_authenticated_request_without_logging_it(self):
        token = self.root / "test-token"
        token.write_text("fixture-only-token")
        token.chmod(0o600)
        destination = backup.Notion(dict(self.cfg, token_file=str(token)))
        with patch("urllib.request.urlopen", return_value=io.BytesIO(b'{"ok":true}')) as request:
            self.assertEqual(destination.request("GET", "/users/me"), {"ok": True})
        self.assertEqual(request.call_args.args[0].get_header("Authorization"), "Bearer fixture-only-token")


if __name__ == "__main__":
    unittest.main()
