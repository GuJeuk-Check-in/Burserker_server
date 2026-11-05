#!/usr/bin/env python3
"""Restore a real archive into a disposable, network-isolated MySQL container."""
import argparse
import gzip
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import time
import uuid

from backup import BackupError, Repository, fingerprint, now, read_json, save_json


def verify(cfg, manifest):
    repository = Repository(cfg)
    manifest = manifest.resolve()
    if not manifest.is_relative_to(repository.root.resolve()):
        raise BackupError("Restore verification must use this backup repository")
    baseline = read_json(manifest)
    if baseline["kind"] != "dump":
        raise BackupError("A full dump is required to verify restore")
    path = repository.archive(manifest, baseline)
    if fingerprint(path)["sha256"] != baseline["sha256"]:
        raise BackupError("Dump checksum mismatch before restore")
    name = "gujeuk-backup-restore-"+uuid.uuid4().hex[:12]
    image = cfg.get("restore_image", "mysql:8.0")
    created = False

    def execute(args, **kwargs):
        return subprocess.run(args, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                              timeout=300, **kwargs).stdout

    def mysql(program="mysql", args=(), stdin=False):
        return ["docker", "exec", *(["-i"] if stdin else []), name, "sh", "-eu", "-c",
                'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec "$@"',
                "isolated-restore", program, "--user=root", "--host=127.0.0.1", "--protocol=tcp", *args]

    try:
        execute(["docker", "run", "-d", "--name", name, "--network", "none", "--memory=768m",
                 "-e", "MYSQL_ROOT_PASSWORD="+uuid.uuid4().hex, image,
                 "--skip-log-bin", "--event-scheduler=OFF", "--innodb-buffer-pool-size=64M",
                 "--performance-schema=OFF"])
        created = True
        for attempt in range(90):
            try:
                execute(mysql(args=("-Nse", "SELECT 1")))
                break
            except subprocess.SubprocessError:
                time.sleep(1)
        else:
            raise BackupError("Isolated restore MySQL did not become ready")
        with gzip.open(path, "rb") as stream:
            with tempfile.TemporaryFile() as errors:
                process = subprocess.Popen(mysql(args=("--binary-mode",), stdin=True),
                                           stdin=subprocess.PIPE, stdout=subprocess.DEVNULL, stderr=errors)
                try:
                    shutil.copyfileobj(stream, process.stdin, 64*1024)
                    process.stdin.close()
                    if process.wait(timeout=300):
                        raise BackupError("Dump could not be restored into isolated MySQL")
                finally:
                    if process.poll() is None:
                        process.kill()
                        process.wait()
        tables = execute(mysql(args=("-Nse", "SELECT TABLE_NAME FROM information_schema.TABLES "
                    "WHERE TABLE_SCHEMA='"+cfg["database"]+"' AND TABLE_TYPE='BASE TABLE'"))).decode().splitlines()
        if not tables:
            raise BackupError("Restore produced no tables")
        for table in tables:
            quoted = "`"+table.replace("`", "``")+"`"
            execute(mysql(args=("-Nse", "SELECT COUNT(*) FROM `"+cfg["database"]+"`."+quoted)))
        baseline.update(restore_verified={"at": now().isoformat(), "database": cfg["database"],
                        "table_count": len(tables)}, summary_synced=False)
        save_json(manifest, baseline)
        print("Isolated full dump restore verified; source database was not modified")
    finally:
        if created:
            execute(["docker", "rm", "-fv", name])


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--config", default=str(Path(__file__).with_name("config.json")))
    args = parser.parse_args()
    try:
        verify(read_json(Path(args.config)), args.manifest)
    except (BackupError, OSError, ValueError, subprocess.SubprocessError) as exc:
        print(str(exc) if isinstance(exc, BackupError) else "Isolated restore failed", file=sys.stderr)
        sys.exit(1)
