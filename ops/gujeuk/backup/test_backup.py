import datetime as dt
import gzip
import fcntl
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import backup


class Source:
    def __init__(self):
        self.dumps, self.fail = 0, False

    def check(self):
        return {"estimated_bytes": 100, "table_count": 1}

    def dump(self, path):
        self.dumps += 1
        with gzip.open(path, "wb") as out:
            out.write(b"CREATE TABLE item(id INT);\n-- Dump completed on 2026-11-06\n")
        if self.fail:
            raise backup.BackupError("dump failed")


class Destination(backup.Destination):
    def __init__(self):
        self.children = [dict(b, id=str(i)) for i, b in enumerate(backup.page_template())]
        self.properties, self.uploads, self.fail_response = {}, 0, False

    def page(self, day):
        return "page"

    def blocks(self, page_id):
        return self.children

    def upload(self, path, meta, save, limit):
        self.uploads += 1
        meta["upload_id"] = "upload"
        save()
        return "upload"

    def request(self, method, endpoint, payload):
        if endpoint.endswith("/children"):
            anchor = payload["position"]["after_block"]["id"]
            index = next(i for i, b in enumerate(self.children) if b["id"] == anchor)
            self.children[index+1:index+1] = payload["children"]
            if self.fail_response:
                raise backup.BackupError("response lost")
        elif endpoint.startswith("/pages/"):
            self.properties = payload["properties"]
        else:
            target = next(b for b in self.children if b.get("id") == endpoint.rsplit("/", 1)[1])
            target.update(payload)


class Backups(unittest.TestCase):
    def setUp(self):
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        self.root, self.source = Path(folder.name), Source()
        self.cfg = {"container": "fixture", "database": "fixture",
                    "backup_directory": str(self.root), "minimum_free_bytes": 0,
                    "deploy_lock": str(self.root / "deploy.lock")}
        self.repo = backup.Repository(self.cfg, self.source)
        self.at = dt.datetime(2026, 11, 6, 10, tzinfo=backup.KST)

    def test_failed_dump_is_not_saved_as_success(self):
        self.source.fail = True
        with self.assertRaises(backup.BackupError):
            self.repo.snapshot(self.at, "hourly-10")
        self.assertFalse(list(self.root.rglob("*.sql.gz")))
        self.assertFalse(list(self.root.rglob("*.partial")))
        self.assertFalse(self.repo.manifests())

    def test_retry_keeps_original_dump(self):
        first = self.repo.snapshot(self.at, "hourly-10")
        self.assertEqual(first, self.repo.snapshot(self.at, "hourly-10"))
        self.assertEqual(self.source.dumps, 1)

    def test_notion_failure_preserves_local_backup(self):
        with patch.object(backup, "MySQL", return_value=self.source), \
             patch.object(backup, "publish_pending", side_effect=backup.BackupError("offline")):
            with self.assertRaises(backup.BackupError):
                backup.run(self.cfg, "hourly", self.at)
        self.assertEqual(len(list(self.root.rglob("*.sql.gz"))), 1)

    def test_retry_never_creates_new_dump(self):
        with patch.object(backup, "MySQL", return_value=self.source), patch.object(backup, "publish_pending"):
            backup.run(self.cfg, "retry", self.at)
        self.assertEqual(self.source.dumps, 0)

    def test_deploy_lock_covers_dump_but_is_released_before_notion(self):
        def lock_available():
            with Path(self.cfg["deploy_lock"]).open("a+") as lock:
                try:
                    fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
                except BlockingIOError:
                    return False
                return True

        dump = self.source.dump

        def capture(path):
            self.assertFalse(lock_available())
            dump(path)

        def publish(*args):
            self.assertTrue(lock_available())

        with patch.object(backup, "MySQL", return_value=self.source), \
             patch.object(self.source, "dump", side_effect=capture), \
             patch.object(backup, "publish_pending", side_effect=publish):
            backup.run(self.cfg, "hourly", self.at)

    def test_retry_skips_old_error_dates_without_deleting_their_details(self):
        errors = {date: {"hourly-10": {"message": "dump failed"}} for date in
                  ("2026-11-04", "2026-11-05", "2026-11-06")}
        self.repo.state["capture_errors"] = errors
        with patch.object(backup, "Destination") as destination:
            backup.publish_pending(self.cfg, self.repo, "2026-11-06")
        self.assertEqual([call.args[1] for call in destination.return_value.update_day.call_args_list],
                         ["2026-11-05", "2026-11-06"])
        self.assertEqual(self.repo.state["capture_errors"], errors)
        self.assertEqual(errors["2026-11-04"]["hourly-10"]["message"], "dump failed")

    def test_schedule_has_nineteen_hours_and_final(self):
        for hour in range(1, 20):
            self.assertEqual(backup.capture_slot("hourly", self.at.replace(hour=hour)), "hourly-%02d" % hour)
        self.assertIsNone(backup.capture_slot("hourly", self.at.replace(hour=22)))
        self.assertEqual(backup.capture_slot("dump", self.at.replace(hour=19, minute=30)), "final")

    def test_lost_response_does_not_duplicate_and_file_is_under_its_hour(self):
        manifest = self.repo.snapshot(self.at, "hourly-10")
        destination = Destination()
        destination.fail_response = True
        with self.assertRaises(backup.BackupError):
            destination.publish(self.repo, manifest, 10000)
        destination.fail_response = False
        destination.publish(self.repo, manifest, 10000)
        files = [b for b in destination.children if b["type"] == "file"]
        self.assertEqual((len(files), destination.uploads), (1, 1))
        index = destination.children.index(files[0])
        self.assertEqual(destination.children[index-1]["heading_3"]["rich_text"][0]["text"]["content"], "10:00")

    def test_missing_backups_go_in_page_body_not_removed_properties(self):
        destination = Destination()
        with patch.object(backup, "now", return_value=self.at.replace(hour=20)):
            destination.update_day(self.repo, "2026-11-06")
        self.assertFalse(set(destination.properties) & {"상태", "시간별 덤프 수", "최종 덤프 시각", "오류 내용"})
        self.assertIn("01:00", destination.children[-1]["paragraph"]["rich_text"][0]["text"]["content"])

    def test_dump_only_targets_configured_database(self):
        with patch("subprocess.run", side_effect=RuntimeError("inspect command")) as call:
            with self.assertRaises(RuntimeError):
                backup.MySQL(self.cfg).dump(self.root / "test.gz")
        command = call.call_args.args[0]
        self.assertEqual(command[-2:], ["--databases", "fixture"])
        self.assertNotIn("--all-databases", command)


if __name__ == "__main__":
    unittest.main()
