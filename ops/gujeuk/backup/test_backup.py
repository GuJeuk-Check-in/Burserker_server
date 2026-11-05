import datetime as dt
import contextlib
import gzip
import io
import ssl
from pathlib import Path
import tempfile
import unittest
import urllib.error
from unittest.mock import patch

import backup
import configure

SQL = b"CREATE TABLE item(id INT);\n-- Dump completed on 2026-11-05\n"


class Source:
    def __init__(self):
        self.dumps, self.fail = 0, False

    def check(self):
        return {"estimated_bytes": 100, "table_count": 1}

    def dump(self, path):
        self.dumps += 1
        with gzip.open(path, "wb") as stream:
            stream.write(SQL)
        if self.fail:
            raise backup.BackupError("fixture failure")


class Backups(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root, self.source = Path(self.temp.name), Source()
        self.cfg = {"container": "fixture", "database": "fixture",
                    "backup_directory": str(self.root), "minimum_free_bytes": 0}
        self.repo = backup.Repository(self.cfg, self.source)
        self.at = dt.datetime(2026, 11, 5, 10, tzinfo=backup.KST)

    def snapshot(self, slot="hourly-10"):
        return self.repo.snapshot(self.at, slot)

    def test_failed_dump_is_not_committed(self):
        self.source.fail = True
        with self.assertRaises(backup.BackupError):
            self.snapshot()
        for pattern in ("*.json", "*.partial", "*.sql.gz"):
            self.assertFalse(list(self.root.rglob(pattern)))

    def test_same_slot_reuses_original_snapshot(self):
        first = self.snapshot()
        original = first.read_bytes()
        self.repo.snapshot(self.at + dt.timedelta(minutes=5), "hourly-10")
        self.assertEqual(self.source.dumps, 1)
        self.assertEqual(first.read_bytes(), original)

    def test_hourly_final_bootstrap_are_independent(self):
        for slot in ("hourly-10", "final", "bootstrap"):
            self.snapshot(slot)
        self.assertEqual(len(self.repo.manifests()), 3)
        self.assertEqual(len(list(self.root.rglob("*.sql.gz"))), 3)

    def test_next_day_has_independent_snapshot(self):
        self.snapshot()
        tomorrow = self.repo.snapshot(self.at + dt.timedelta(days=1), "hourly-10")
        self.assertEqual(tomorrow.parent.name, "2026-11-06")
        self.assertEqual(self.source.dumps, 2)

    def test_tampered_archive_is_rejected_before_reuse(self):
        manifest = self.snapshot()
        meta = backup.read_json(manifest)
        with gzip.open(manifest.parent/meta["filename"], "wb") as stream:
            stream.write(SQL+b"unexpected data")
        with self.assertRaisesRegex(backup.BackupError, "checksum"):
            self.snapshot()

    def test_invalid_archive_path_is_rejected(self):
        with self.assertRaises(backup.BackupError):
            self.repo.archive(self.root / "hourly-10.json", {"filename": "../outside.gz"})

    def test_disk_full_preserves_previous_snapshot(self):
        self.snapshot()
        with patch.object(backup.shutil, "disk_usage") as usage:
            usage.return_value.free = 0
            with self.assertRaisesRegex(backup.BackupError, "disk space"):
                self.snapshot("hourly-11")
        self.assertEqual(len(self.repo.manifests()), 1)

    def test_notion_outage_does_not_prevent_local_dump(self):
        with patch.object(backup, "MySQL", return_value=self.source), \
             patch.object(backup, "Destination", side_effect=backup.BackupError("offline")):
            with self.assertRaises(backup.BackupError):
                backup.run(self.cfg, "hourly", self.at)
        self.assertEqual(len(list(self.root.rglob("*.sql.gz"))), 1)

    def test_retry_does_not_capture(self):
        with patch.object(backup, "MySQL", return_value=self.source), patch.object(backup, "publish_pending"):
            backup.run(self.cfg, "retry", self.at)
        self.assertEqual(self.source.dumps, 0)

    def test_late_restart_does_not_invent_historical_slots(self):
        self.assertIsNone(backup.capture_slot("hourly", self.at.replace(hour=22)))
        self.assertEqual(backup.capture_slot("dump", self.at.replace(hour=22)), "final")
        self.assertIsNone(backup.capture_slot("dump", self.at.replace(hour=0)))
        self.assertEqual(backup.capture_slot("hourly", self.at.replace(hour=15, minute=12)), "hourly-15")

    def test_all_nineteen_hours_and_final_schedule(self):
        for hour in range(1, 20):
            self.assertEqual(backup.capture_slot("hourly", self.at.replace(hour=hour)), "hourly-%02d" % hour)
        self.assertIsNone(backup.capture_slot("hourly", self.at.replace(hour=19, minute=30)))
        self.assertEqual(backup.capture_slot("dump", self.at.replace(hour=19, minute=30)), "final")

    def test_later_success_does_not_erase_earlier_failure(self):
        with patch.object(backup, "MySQL", return_value=self.source), patch.object(backup, "publish_pending"):
            self.source.fail = True
            with self.assertRaises(backup.BackupError):
                backup.run(self.cfg, "hourly", self.at)
            self.source.fail = False
            backup.run(self.cfg, "hourly", self.at.replace(hour=11))
        self.assertIn("hourly-10", backup.read_json(self.root / "state.json")["capture_errors"]["2026-11-05"])

    def test_missing_slots_cannot_be_reported_as_complete(self):
        manifest = self.snapshot("final")
        meta = backup.read_json(manifest)
        meta.update(notion_attached=True, upload_id="fixture", restore_verified=True)
        backup.save_json(manifest, meta)
        destination = object.__new__(backup.Destination)
        with patch.object(destination, "page", return_value="fixture"), \
             patch.object(destination, "request") as request, \
             patch.object(backup, "now", return_value=self.at.replace(hour=20)):
            destination.update_day(self.repo, "2026-11-05")
        props = request.call_args.args[2]["properties"]
        self.assertEqual(props["상태"]["select"]["name"], "오류")
        self.assertEqual(props["시간별 덤프 수"]["number"], 0)

    def test_lost_attachment_response_does_not_duplicate(self):
        class Destination(backup.Destination):
            def __init__(self):
                self.children, self.appends, self.uploads = [], 0, 0
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
                    raise backup.BackupError("Lost response after attachment accepted")

        manifest, destination = self.snapshot(), Destination()
        with self.assertRaises(backup.BackupError):
            destination.publish(self.repo, manifest, 10000)
        destination.lose_response = False
        destination.publish(self.repo, manifest, 10000)
        self.assertEqual((destination.appends, destination.uploads), (1, 1))
        self.assertTrue(backup.read_json(manifest)["notion_attached"])

    def test_dump_targets_one_database_without_global_coordinates(self):
        with patch("subprocess.Popen", side_effect=RuntimeError("captured")) as process:
            with self.assertRaises(RuntimeError):
                backup.MySQL(self.cfg).dump(self.root / "fixture.partial")
        command = process.call_args.args[0]
        self.assertEqual(command[-2:], ["--databases", "fixture"])
        self.assertFalse(any("source-data" in arg or "all-databases" in arg for arg in command))

    def test_private_token_used_without_output(self):
        token = self.root / "test-token"
        token.write_text("fixture-only-token")
        token.chmod(0o600)
        destination = backup.Notion(dict(self.cfg, token_file=str(token)))
        with patch("urllib.request.urlopen", return_value=io.BytesIO(b'{"ok":true}')) as request:
            self.assertEqual(destination.request("GET", "/users/me"), {"ok": True})
        self.assertEqual(request.call_args.args[0].get_header("Authorization"), "Bearer fixture-only-token")

    def test_untrusted_ca_has_an_actionable_error_without_token(self):
        token = self.root / "test-token"
        token.write_text("fixture-only-token")
        token.chmod(0o600)
        destination = backup.Notion(dict(self.cfg, token_file=str(token)))
        failure = urllib.error.URLError(ssl.SSLCertVerificationError("untrusted CA"))
        with patch("urllib.request.urlopen", side_effect=failure):
            with self.assertRaisesRegex(backup.BackupError, "SSL_CERT_FILE") as raised:
                destination.request("GET", "/users/me")
        self.assertNotIn("fixture-only-token", str(raised.exception))

    def test_data_source_is_resolved_from_the_api_database_response(self):
        destination = object.__new__(backup.Notion)
        database_id = "11111111-1111-4111-8111-111111111111"
        source_id = "22222222-2222-4222-8222-222222222222"
        with patch.object(destination, "request", return_value={
                "data_sources": [{"id": source_id, "name": "fixture"}]}) as request:
            self.assertEqual(destination.resolve_data_source(database_id), source_id)
        request.assert_called_once_with("GET", "/databases/"+database_id)

    def test_empty_or_ambiguous_data_source_list_is_not_guessed(self):
        destination = object.__new__(backup.Notion)
        for sources in ([], [{"id": "one"}, {"id": "two"}]):
            with self.subTest(sources=sources), patch.object(destination, "request",
                    return_value={"data_sources": sources}):
                with self.assertRaisesRegex(backup.BackupError, "exactly one"):
                    destination.resolve_data_source("11111111-1111-4111-8111-111111111111")

    def test_setup_with_database_id_saves_api_resolved_source_without_token_output(self):
        setup = self.root / "setup"
        source_id = "22222222-2222-4222-8222-222222222222"
        output = io.StringIO()
        with patch("sys.argv", ["configure.py", "--root", str(setup), "--database-id",
                    "11111111-1111-4111-8111-111111111111"]), \
             patch("builtins.input", side_effect=AssertionError("No ID prompt expected")), \
             patch("getpass.getpass", return_value="fixture-only-token"), \
             patch.object(configure, "Notion") as client, contextlib.redirect_stdout(output):
            client.return_value.request.return_value = {"name": "fixture-connection"}
            client.return_value.resolve_data_source.return_value = source_id
            configure.main()
        saved = backup.read_json(setup / "config.json")
        self.assertEqual(saved["data_source_id"], source_id)
        self.assertEqual((setup / "private/notion-token").stat().st_mode & 0o777, 0o600)
        self.assertNotIn("fixture-only-token", output.getvalue())

    def test_failed_database_access_does_not_leave_private_token_or_config(self):
        setup = self.root / "setup"
        with patch("sys.argv", ["configure.py", "--root", str(setup), "--database-id",
                    "11111111-1111-4111-8111-111111111111"]), \
             patch("getpass.getpass", return_value="fixture-only-token"), \
             patch.object(configure, "Notion") as client, contextlib.redirect_stdout(io.StringIO()):
            client.return_value.request.return_value = {"name": "fixture-connection"}
            client.return_value.resolve_data_source.side_effect = backup.BackupError("HTTP 404")
            with self.assertRaises(SystemExit):
                configure.main()
        self.assertFalse((setup / "config.json").exists())
        self.assertFalse(list((setup / "private").iterdir()))


if __name__ == "__main__":
    unittest.main()
