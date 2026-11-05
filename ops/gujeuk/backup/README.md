# 구즉 운영 DB → Notion 자동 백업

EC2의 systemd가 실행한다. 노트북 전원·로그인·SSH 연결 유지에 의존하지 않는다. Notion에는 데이터베이스 하나와 날짜별 페이지 하나를 유지한다. Python 3.10 이상과 Docker가 필요하며 추가 Python 패키지는 없다.

## 일정 (Asia/Seoul)

| 시각 | 작업 |
| --- | --- |
| 00:00 | 당일 페이지 생성 |
| 01:00~19:00 매 정각 | 마지막 성공 위치 이후의 닫힌 binlog 수집·검증·업로드 |
| 19:30 | 미보관 binlog 수집 → 전체 덤프 → 덤프 중 발생한 binlog 추가 수집·업로드 |
| 매 5분 | 기존 파일·첨부 상태 재확인 및 미완료 업로드 재시도. 새 덤프·binlog는 생성하지 않음 |

시각은 **시작 시각**이다. binlog 기록은 MySQL에서 24시간 유지하고, 다음 수집이 야간 변경까지 이어 받는다. 서버 재부팅 등으로 놓친 예약은 재가동 때 한 번 실행하며 실제 실행 날짜·시각을 기록한다. 과거 시점의 덤프를 만들어 낸 것으로 기록하지 않는다.

한 시간 간격 보관은 무손실 보장이 아니다. 정상 동작 중에도 마지막 외부 보관 이후의 변경은 유실될 수 있고, 19:30 이후에는 다음 binlog 업로드 전까지 노출 구간이 더 길다. 마지막 성공한 전체 덤프와 그 이후의 **연속된** binlog가 복구에 필요하다.

## 수집 범위와 운영 영향

- 전체 덤프: `config.json`의 단일 DB. 기본값 `gujeuk_prod`.
- 원본 binlog: MySQL 인스턴스 전체. 같은 MySQL의 `gujuck_game` 등 다른 DB 변경도 포함될 수 있다. 승인 후에만 `allow_instance_binlogs=true`로 설정한다. `false`면 수집을 거부한다.
- 운영 데이터 INSERT/UPDATE/DELETE·복원·컨테이너 재시작은 하지 않는다. `FLUSH BINARY LOGS`로 파일을 닫고, 체크섬을 확인한 파일만 읽는다.
- MySQL 서버 이미지에 `mysqlbinlog`가 없으므로 공식 `mysql:8.0-debian` 도구 컨테이너를 잠깐 실행한다. 소스 볼륨은 읽기 전용, 네트워크는 차단한다. 대상 EC2는 x86_64이며 도구는 `linux/amd64`로 실행한다.
- `--single-transaction --source-data=2` 덤프는 InnoDB 스냅샷과 복구 좌표를 저장한다. 시작 시 짧은 전역 읽기 잠금이 발생할 수 있다. 배포와는 기존 `.deploy.lock`으로 직렬화한다.
- binlog가 꺼져 있거나 ROW가 아니면 실패한다. 원본 누락·인스턴스 UUID 변경·해시 불일치도 건너뛰지 않는다. 기존 MySQL 설정을 자동 변경하거나 재시작하지 않는다.
- `sync_binlog=1`, `innodb_flush_log_at_trx_commit=1`을 확인한다. binlog 보존 기간보다 수집 장애가 길면 복구 체인이 끊길 수 있다. 원본 binlog를 임의 삭제하지 않는다.

## 최초 설치

실제 Notion 내부 연결 토큰은 GitHub·채팅·페이지 본문에 넣지 않는다. 개인 워크스페이스에서 콘텐츠 읽기·삽입·수정 권한의 내부 연결을 만들고 **백업 상위 페이지에만** 연결한다. 개인 Notion 로그인/MCP 연결은 EC2 토큰을 대신하지 않는다.

이 디렉터리를 EC2에 복사하거나 검토한 브랜치를 EC2에 checkout한 뒤 `ubuntu` 사용자로 실행한다.

```bash
bash ops/gujeuk/backup/install.sh --prepare
docker pull --platform linux/amd64 mysql:8.0-debian
docker pull mysql:8.0
python3 /home/ubuntu/gujeuk-notion-backup/configure.py
```

`configure.py`에 백업 데이터 소스 ID(`collection://` 뒤의 UUID)를 입력한다. 부모 페이지 ID·데이터베이스 ID와 다르다. 원본 binlog 범위를 확인하고 토큰을 숨김 입력한다. 실제 읽기 접근·업로드 용량 제한을 확인한 뒤 권한 600 파일과 비공개 `config.json`을 저장한다. 기존 토큰·설정은 덮어쓰지 않는다. 이 단계는 타이머를 활성화하지 않는다.

```bash
cd /home/ubuntu/gujeuk-notion-backup
python3 backup.py check
python3 backup.py bootstrap
python3 backup.py binlog
```

최초 기준 덤프를 먼저 확보하고, 실제 binlog가 Notion에 첨부됐는지 확인한다. `data/YYYY-MM-DD/baseline.json`은 첨부한 덤프·좌표·해시를 기록한다. 실제 날짜로 다음 명령의 경로를 바꾼다.

```bash
python3 verify_restore.py data/YYYY-MM-DD/baseline.json
python3 backup.py retry
bash install.sh --enable
systemctl list-timers 'gujeuk-backup-*' --no-pager
```

`verify_restore.py`는 포트·네트워크가 없는 일회용 MySQL에 실제 덤프를 복원하고, 구즉 DB 대상으로 binlog를 재생한 뒤 모든 테이블을 조회한다. 이 임시 컨테이너와 볼륨만 삭제하며 운영 컨테이너에는 복원하지 않는다. 실제 백업 내용이 복원되는 동안 추가 RAM 768MiB 및 데이터 용량만큼의 디스크 여유가 필요하다. 운영 서버 자원이 부족하면 별도 장비에서 검증한다.

최초 덤프·binlog의 실제 첨부와 복원 확인 기록이 없으면 설치 프로그램이 타이머 활성화를 거부한다. 코드 머지만으로 타이머가 설치되거나 실행되지 않는다. 토큰과 서버 준비 뒤 최초 한 번 설치해야 한다.

## 파일과 실패 처리

- 토큰: `private/notion-token` (소유 사용자 전용, 권한 600).
- 백업/메타데이터: `data/YYYY-MM-DD/` (권한 700). 종료 코드·완료 표식·gzip·binlog 체크섬·SHA-256을 확인하고 원자적으로 확정한다.
- Notion API 오류보다 로컬 수집을 먼저 수행한다. 실패 파일은 삭제하지 않는다. 첨부 요청 응답이 끊겨도 파일명+SHA-256 표식을 조회하여 중복 첨부를 피한다.
- Notion 파일 업로드 세션이 미완료면 만료 후 새 업로드를 시도한다. 복원에 필요한 각 파일의 위치와 해시는 페이지 본문에도 남긴다. 파일 속성의 100개 제한을 넘으면 본문 첨부를 사용한다.
- 무료 Notion은 파일당 5MiB, 유료는 5GiB 제한을 실제 봇 응답으로 확인한다. 한도 초과는 성공으로 처리하지 않는다. gzip은 암호화가 아니다. 백업에는 개인정보가 포함되므로 상위 페이지의 공유 범위를 확인한다.
- 기본값으로 백업을 자동 삭제하지 않는다. 디스크 여유 512MiB 미만에서 수집을 중단하므로 보관 용량과 업로드 실패를 감시해야 한다.
- 소스 수집 실패는 systemd에서 5분 간격으로 제한 재시도한다. 원인 해결 뒤 해당 모드(`binlog`/`dump`)를 재실행한다. 업로드 재시도는 소스 수집을 대신하지 않는다.
- 같은 서버 전체가 중지되면 Notion에 실패를 기록할 수 없다. 외부 누락 감시는 별도로 운영해야 한다.

```bash
journalctl -u 'gujeuk-backup@*' -n 50 --no-pager
systemctl --failed
sudo systemctl start gujeuk-backup@binlog.service
sudo systemctl start gujeuk-backup@retry.service
```

## 검증

```bash
cd ops/gujeuk/backup
python3 -m unittest -v test_backup.py
python3 test_restore.py
```

단위 테스트는 실패 후 커서 보존, 원본 누락 거부, 날짜 경계·야간 연속성, 인스턴스 변경 거부, 업로드 장애 중 로컬 보관 등을 검사한다. Docker 테스트는 임시 DB에 실제 덤프와 binlog를 복원하고, 마지막 덤프 이후 데이터와 복구 시점을 구분해 확인한다. 이는 운영 데이터의 실제 복원 검증을 대신하지 않는다.
