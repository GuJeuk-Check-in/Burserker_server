#!/usr/bin/env python3
"""Real MySQL 8.0 recovery test. Only disposable fixture containers are touched."""
import gzip
import json
from pathlib import Path
import subprocess
import tempfile
import time
import uuid

import backup


def docker(*args, input=None):
    return subprocess.run(["docker", *args], input=input, capture_output=True,
                          check=True, timeout=180).stdout


def client(container, program="mysql", args=(), stdin=False):
    command = ["docker", "exec"] + (["-i"] if stdin else [])
    return command + [container, "sh", "-eu", "-c",
                      'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec "$@"',
                      "fixture-client", program, "--user=root", "--host=127.0.0.1", "--protocol=tcp", *args]


def sql(container, query):
    return subprocess.run(client(container, args=("-Nse", query)),
                          capture_output=True, check=True, timeout=30).stdout


def restore_dump(container, path):
    with gzip.open(path, "rb") as stream:
        subprocess.run(client(container, args=("--binary-mode",), stdin=True),
                       input=stream.read(), stdout=subprocess.DEVNULL,
                       stderr=subprocess.PIPE, check=True, timeout=120)


def main():
    names = ["gujeuk-backup-fixture-"+uuid.uuid4().hex[:12] for _ in range(2)]
    started = []
    try:
        for name in names:
            docker("run", "-d", "--name", name, "--memory=768m",
                   "-e", "MYSQL_ROOT_PASSWORD=fixture-only-password", "mysql:8.0",
                   "--server-id=166", "--log-bin=fixture-bin", "--binlog-format=ROW",
                   "--innodb-buffer-pool-size=64M", "--performance-schema=OFF")
            started.append(name)
            for attempt in range(90):
                try:
                    sql(name, "SELECT 1")
                    break
                except subprocess.SubprocessError:
                    time.sleep(1)
            else:
                raise RuntimeError("Fixture MySQL did not become ready")
        source, target = names
        sql(source, "CREATE DATABASE backup_fixture; "
                    "CREATE TABLE backup_fixture.item(id INT PRIMARY KEY) ENGINE=InnoDB; "
                    "INSERT INTO backup_fixture.item VALUES(1)")
        with tempfile.TemporaryDirectory(prefix="gujeuk-backup-restore-") as folder:
            cfg = {"container": source, "database": "backup_fixture",
                   "backup_directory": folder, "minimum_free_bytes": 0,
                   "allow_instance_binlogs": True}
            repo = backup.Repository(cfg)
            baseline = repo.snapshot(backup.now(), baseline=True)
            sql(source, "INSERT INTO backup_fixture.item VALUES(2)")
            logs = repo.collect(backup.now())
            sql(source, "INSERT INTO backup_fixture.item VALUES(3)")
            base = json.loads(baseline.read_text())
            restore_dump(target, baseline.parent / base["filename"])
            assert sql(target, "SELECT COUNT(*) FROM backup_fixture.item").strip() == b"1"
            for manifest in logs:
                meta = json.loads(manifest.read_text())
                raw = Path(folder) / meta["log_file"]
                with gzip.open(manifest.parent / meta["filename"], "rb") as stream:
                    raw.write_bytes(stream.read())
                decoded = docker("run", "--rm", "--platform", "linux/amd64", "--network", "none",
                                 "--mount",
                                 "type=bind,src="+folder+",dst=/backup,readonly",
                                 "--entrypoint", "mysqlbinlog", "mysql:8.0-debian", "--no-defaults",
                                 "--verify-binlog-checksum",
                                 "--skip-gtids", "--disable-log-bin", "--database=backup_fixture",
                                 "--start-position="+str(meta["start_position"]),
                                 "/backup/"+meta["log_file"])
                subprocess.run(client(target, args=("--binary-mode",), stdin=True),
                               input=decoded, stdout=subprocess.DEVNULL,
                               stderr=subprocess.PIPE, check=True, timeout=120)
            # The third row is deliberately newer than the archived logs.
            assert sql(target, "SELECT GROUP_CONCAT(id ORDER BY id) FROM backup_fixture.item").strip() == b"1,2"
            from verify_restore import verify
            verify(cfg, baseline)
            assert json.loads(baseline.read_text())["restore_verified"]["binlog_count"] > 0
            final = repo.snapshot(backup.now())
            latest = json.loads(final.read_text())
            restore_dump(target, final.parent / latest["filename"])
            assert sql(target, "SELECT COUNT(*) FROM backup_fixture.item").strip() == b"3"
            print("PASS: full dump restores 1 row; dump + archived binlogs restores 2; final dump restores 3")
    finally:
        for name in started:
            docker("rm", "-fv", name)


if __name__ == "__main__":
    main()
