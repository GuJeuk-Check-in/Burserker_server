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
            docker("run", "-d", "--name", name, "--network", "none", "--memory=768m",
                   "-e", "MYSQL_ROOT_PASSWORD=fixture-only-password", "mysql:8.0",
                   "--skip-log-bin", "--innodb-buffer-pool-size=64M", "--performance-schema=OFF")
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
                    "INSERT INTO backup_fixture.item VALUES(1); "
                    "CREATE DATABASE other_private_db; "
                    "CREATE TABLE other_private_db.secret(value TEXT); "
                    "INSERT INTO other_private_db.secret VALUES('other-db-sentinel-must-not-be-backed-up')")
        with tempfile.TemporaryDirectory(prefix="gujeuk-backup-restore-") as folder:
            cfg = {"container": source, "database": "backup_fixture",
                   "backup_directory": folder, "minimum_free_bytes": 0}
            repo = backup.Repository(cfg)
            hourly = repo.snapshot(backup.now(), "hourly-10")
            sql(source, "INSERT INTO backup_fixture.item VALUES(2)")
            final = repo.snapshot(backup.now(), "final")
            for manifest, expected in ((hourly, b"1"), (final, b"2")):
                meta = backup.read_json(manifest)
                path = manifest.parent / meta["filename"]
                with gzip.open(path, "rb") as stream:
                    content = stream.read()
                assert b"other_private_db" not in content
                assert b"other-db-sentinel" not in content
                restore_dump(target, path)
                assert sql(target, "SELECT COUNT(*) FROM backup_fixture.item").strip() == expected
                assert sql(target, "SELECT COUNT(*) FROM information_schema.SCHEMATA "
                           "WHERE SCHEMA_NAME='other_private_db'").strip() == b"0"
            print("PASS: independent hourly/final dumps restore 1/2 rows; other database excluded")
    finally:
        for name in started:
            docker("rm", "-fv", name)


if __name__ == "__main__":
    main()
