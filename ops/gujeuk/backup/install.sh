#!/usr/bin/env bash
set -euo pipefail
umask 077
source_dir="$(cd "$(dirname "$0")" && pwd)"
target_dir=/home/ubuntu/gujeuk-notion-backup

if [ "$(id -un)" != ubuntu ]; then
  echo 'Run as ubuntu on the Gujeuk EC2.' >&2
  exit 1
fi

case "${1:---prepare}" in
  --prepare)
    install -d -m 700 "$target_dir" "$target_dir/data" "$target_dir/private" "$target_dir/systemd"
    if [ "$source_dir" != "$target_dir" ]; then
      install -m 600 "$source_dir"/{backup.py,backup_core.py,configure.py,verify_restore.py,config.example.json} "$target_dir/"
      install -m 600 "$source_dir"/systemd/* "$target_dir/systemd/"
    fi
    echo 'Programs prepared; no token overwritten and no timer enabled.'
    echo "Next: python3 $target_dir/configure.py"
    ;;
  --enable)
    python3 "$target_dir/backup.py" check
    python3 "$target_dir/backup.py" verify
    python3 - "$target_dir" <<'PY'
import json, pathlib, sys
root = pathlib.Path(sys.argv[1])
cfg = json.loads((root / 'config.json').read_text())
data = pathlib.Path(cfg['backup_directory'])
state = json.loads((data / 'state.json').read_text())
if not cfg.get('allow_instance_binlogs') or not state.get('cursor'):
    sys.exit('Confirm binlog scope and create the initial baseline first.')
items = [json.loads(p.read_text()) for p in data.glob('????-??-??/*.json')]
if not any(m.get('baseline') and m.get('notion_attached') for m in items):
    sys.exit('Initial full dump has not been attached to Notion.')
if not any(m['kind'] == 'binlog' and m.get('notion_attached') for m in items):
    sys.exit('No real binlog attachment has been verified.')
if state.get('capture_errors'):
    sys.exit('Resolve source capture errors before enabling timers.')
if not any(m.get('baseline') and m.get('restore_verified') for m in items):
    sys.exit('Verify the real baseline + binlog chain in isolated MySQL before enabling timers.')
PY
    sudo install -m 644 "$target_dir"/systemd/* /etc/systemd/system/
    sudo systemctl daemon-reload
    sudo systemctl enable --now gujeuk-backup-{page,binlog,dump,retry}.timer
    systemctl list-timers 'gujeuk-backup-*' --no-pager
    ;;
  *) echo 'Usage: bash install.sh --prepare | --enable' >&2; exit 2 ;;
esac
