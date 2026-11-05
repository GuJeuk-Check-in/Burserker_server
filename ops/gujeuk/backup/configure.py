#!/usr/bin/env python3
"""Interactive local token entry; never put credentials in shell arguments."""
import argparse
import getpass
import json
import os
from pathlib import Path
import sys
import tempfile
import uuid

from backup_core import Notion, BackupError, secure_directory, save_json


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default="/home/ubuntu/gujeuk-notion-backup")
    args = parser.parse_args()
    root = Path(args.root)
    os.umask(0o077)
    secure_directory(root)
    private = root / "private"
    secure_directory(private)
    config_path = root / "config.json"
    if config_path.exists() or (private / "notion-token").exists():
        sys.exit("Private configuration already exists; edit/rotate it explicitly.")
    config = json.loads((Path(__file__).parent / "config.example.json").read_text())
    config.update(backup_directory=str(root / "data"), token_file=str(private / "notion-token"))
    value = input("Notion data source ID (not the parent page/database ID): ").strip()
    config["data_source_id"] = str(uuid.UUID(value.removeprefix("collection://")))
    token = getpass.getpass("Notion internal connection token (hidden): ").strip()
    if not token or "\n" in token:
        sys.exit("No valid token entered.")
    fd, filename = tempfile.mkstemp(dir=private, prefix=".token-")
    trial = Path(filename)
    try:
        with os.fdopen(fd, "w") as stream:
            stream.write(token)
            stream.flush()
            os.fsync(stream.fileno())
        Notion(dict(config, token_file=str(trial))).preflight()
        os.replace(trial, Path(config["token_file"]))
        save_json(config_path, config)
        print("Notion access checked. Private configuration saved; no timer enabled.")
    except BackupError as exc:
        sys.exit(str(exc))
    finally:
        trial.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
