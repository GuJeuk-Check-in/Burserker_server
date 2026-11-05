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

from backup import BackupError, Repository, archive_fingerprint, now, read_json, save_json


def verify(cfg, manifest):
    repository = Repository(cfg)
    manifest = manifest.resolve()
    if not manifest.is_relative_to(repository.root.resolve()):
        raise BackupError("Restore verification must use this backup repository")
    baseline = read_json(manifest)
    if baseline["kind"] != "dump":
        raise BackupError("A full dump is required to verify restore")
    path = manifest.parent / baseline["filename"]
    if archive_fingerprint(path, "dump")["sha256"] != baseline["sha256"]:
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
        candidates = {}
        for item in repository.manifests():
            meta = read_json(item)
            if (meta["kind"] != "binlog" or meta["server_uuid"] != baseline["server_uuid"] or
                    meta["log_file"] < baseline["position"]["file"]):
                continue
            old = candidates.get(meta["log_file"])
            if old and old[0]["sha256"] != meta["sha256"]:
                raise BackupError("Conflicting archives for the same binlog")
            candidates[meta["log_file"]] = (meta, item)
        if not candidates or baseline["position"]["file"] not in candidates:
            raise BackupError("Archive the baseline's binlog before verifying the restore chain")
        with tempfile.TemporaryDirectory(prefix="gujeuk-restore-binlogs-") as folder:
            for filename, (meta, item) in sorted(candidates.items()):
                archive = item.parent / meta["filename"]
                if archive_fingerprint(archive, "binlog")["sha256"] != meta["sha256"]:
                    raise BackupError("Binary log checksum mismatch before restore")
                raw = Path(folder) / filename
                with gzip.open(archive, "rb") as stream, raw.open("wb") as out:
                    shutil.copyfileobj(stream, out)
                start = baseline["position"]["position"] if filename == baseline["position"]["file"] else 4
                command = ["docker", "run", "--rm", "--platform", "linux/amd64", "--network", "none",
                           "--mount", "type=bind,src="+folder+",dst=/backup,readonly",
                           "--entrypoint", "mysqlbinlog", cfg.get("mysqlbinlog_image", "mysql:8.0-debian"),
                           "--no-defaults", "--verify-binlog-checksum", "--skip-gtids", "--disable-log-bin",
                           "--database="+cfg["database"], "--start-position="+str(start), "/backup/"+filename]
                with tempfile.TemporaryFile() as errors:
                    decoder = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=errors)
                    try:
                        apply = subprocess.run(mysql(args=("--binary-mode",), stdin=True), stdin=decoder.stdout,
                                               stdout=subprocess.DEVNULL, stderr=errors, timeout=300)
                        decoder.stdout.close()
                        if apply.returncode or decoder.wait(timeout=30):
                            raise BackupError("Binary log replay failed in isolated MySQL")
                    finally:
                        if decoder.poll() is None:
                            decoder.kill()
                            decoder.wait()
                raw.unlink()
        tables = execute(mysql(args=("-Nse", "SELECT TABLE_NAME FROM information_schema.TABLES "
                    "WHERE TABLE_SCHEMA='"+cfg["database"]+"' AND TABLE_TYPE='BASE TABLE'"))).decode().splitlines()
        if not tables:
            raise BackupError("Restore produced no tables")
        for table in tables:
            quoted = "`"+table.replace("`", "``")+"`"
            execute(mysql(args=("-Nse", "SELECT COUNT(*) FROM `"+cfg["database"]+"`."+quoted)))
        baseline.update(restore_verified={"at": now().isoformat(), "database": cfg["database"],
                        "table_count": len(tables), "binlog_count": len(candidates)}, summary_synced=False)
        save_json(manifest, baseline)
        print("Isolated dump + binlog restore verified; source database was not modified")
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
