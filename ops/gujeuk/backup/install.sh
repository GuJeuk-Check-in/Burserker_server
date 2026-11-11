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
      install -m 600 "$source_dir"/{backup.py,backup_core.py,configure.py,install.sh,config.example.json} "$target_dir/"
      install -m 600 "$source_dir"/systemd/* "$target_dir/systemd/"
    fi
    echo 'Programs prepared; no token overwritten and no timer enabled.'
    echo "Next: python3 $target_dir/configure.py"
    ;;
  --enable)
    python3 "$target_dir/backup.py" check
    sudo install -m 644 "$target_dir"/systemd/* /etc/systemd/system/
    sudo systemctl daemon-reload
    sudo systemctl enable --now gujeuk-backup-{page,hourly,dump,retry}.timer
    systemctl list-timers 'gujeuk-backup-*' --no-pager
    ;;
  *) echo 'Usage: bash install.sh --prepare | --enable' >&2; exit 2 ;;
esac
