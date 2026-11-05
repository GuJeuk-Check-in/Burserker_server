# 구즉 운영 DB → Notion 자동 백업

EC2의 systemd가 실행한다. 노트북 전원·로그인·SSH 연결 유지에 의존하지 않는다. Notion에는 데이터베이스 하나와 날짜별 페이지 하나를 유지한다. Python 3.10 이상과 Docker가 필요하며 추가 Python 패키지는 없다.

## 일정 (Asia/Seoul)

| 시각 | 작업 |
| --- | --- |
| 00:00 | 당일 페이지 생성 |
| 01:00~19:00 매 정각 | 구즉 운영 DB만 전체 덤프·검증·업로드 (19개) |
| 19:30 | 최종 전체 덤프·검증·업로드 (1개) |
| 매 5분 | 기존 파일·첨부 상태 및 누락 재확인, 미완료 업로드 재시도. 새 덤프 생성 없음 |

시각은 시작 시각이며 완료 시각도 별도로 기록한다. 같은 슬롯의 성공 파일은 재사용하므로 업로드 실패로 기존 백업을 덮어쓰지 않는다. 재부팅으로 놓친 예약은 재가동 때 한 번 실행한다. 시간별 작업은 현재 시각이 01:00~19:29인 경우 현재 시간 슬롯만 생성하고, 최종 작업은 당일 19:30 이후일 때만 생성한다. 과거 시점의 데이터를 소급 생성하지 않는다. 예를 들어 22:00 재기동은 22:00 기준 최종 덤프를 남길 수 있지만 놓친 정각 파일을 만들어 내지는 않는다.

정상 일정은 하루 20개의 독립 전체 덤프다. 19:30 이후 슬롯이나 업로드 누락이 있으면 Notion 상태를 오류로 표시하고 빠진 슬롯을 기록한다. 설치 첫날의 이미 지난 시각도 누락으로 표시될 수 있다. 다음날에는 전날의 미완료 상태도 정리한다. 오류 표시는 외부 알림 전송 기능을 뜻하지 않는다.

한 시간 간격 스냅샷은 무손실 보장이 아니다. 마지막 스냅샷 이후의 변경은 포함되지 않고, 19:30 이후에는 다음 날 01:00까지 간격이 길다. 복구하려는 시점 이전의 전체 덤프 하나로 그 스냅샷 상태를 복원한다. 임의 시점 복구(PITR)는 제공하지 않는다.

## 범위와 운영 영향

- 덤프는 `config.json`의 단일 DB만 대상이며 기본값은 `gujeuk_prod`다. 다른 DB와 binlog는 수집하지 않는다.
- 운영 데이터 INSERT/UPDATE/DELETE·복원·컨테이너 재시작·binlog 회전을 하지 않는다. 덤프 읽기로 DB CPU·디스크·네트워크 부하는 발생할 수 있다.
- `--single-transaction --quick`으로 InnoDB 일관 스냅샷을 만든다. 복구 좌표 옵션과 전역 읽기 잠금은 요청하지 않는다. 비 InnoDB 테이블은 거부하며, 배포에 따른 DDL은 기존 `.deploy.lock`과 직렬화한다. 수동 DDL도 백업과 겹치지 않도록 운영해야 한다.
- 원본의 binlog 설정·보존 기간을 변경하지 않으며 별도 mysqlbinlog 컨테이너도 필요 없다.

## 최초 설치

Notion 개인 워크스페이스에 콘텐츠 읽기·삽입·수정 권한의 내부 연결을 만들고 백업 상위 페이지에만 접근을 허용한다. Notion 로그인/MCP 연결은 EC2 토큰을 대신하지 않는다. 토큰은 GitHub·채팅·페이지 본문에 입력하지 않는다.

검토한 코드를 EC2에 복사한 뒤 `ubuntu` 사용자로 실행한다.

```bash
bash ops/gujeuk/backup/install.sh --prepare
docker pull mysql:8.0
python3 /home/ubuntu/gujeuk-notion-backup/configure.py
```

데이터 소스 ID(`collection://` 뒤 UUID)를 입력하고 연결 토큰을 숨김 입력한다. 부모 페이지·데이터베이스 ID와 다르다. 실제 접근과 파일 업로드 한도를 확인한 뒤 권한 600 설정·토큰 파일을 저장한다. 기존 토큰을 덮어쓰거나 타이머를 활성화하지 않는다.

```bash
cd /home/ubuntu/gujeuk-notion-backup
python3 backup.py check
python3 backup.py bootstrap
python3 verify_restore.py data/YYYY-MM-DD/bootstrap.json
python3 backup.py retry
bash install.sh --enable
systemctl list-timers 'gujeuk-backup-*' --no-pager
```

`YYYY-MM-DD`를 실제 날짜로 바꾼다. `bootstrap`은 설치 검증용 최초 덤프이며 예약된 시간별·최종 덤프와 별개다. `verify_restore.py`는 네트워크와 게시 포트가 없는 일회용 MySQL에 덤프를 복원하고 모든 테이블을 조회한다. 임시 컨테이너와 그 볼륨만 삭제하며 운영 DB에 복원하지 않는다. 추가 RAM 768MiB와 복원 데이터만큼 디스크가 필요하므로 운영 서버 자원이 부족하면 별도 장비에서 검증한다.

실제 덤프 첨부와 격리 복원 확인 기록이 없으면 설치 프로그램은 활성화를 거부한다. 코드 머지만으로 타이머가 설치되거나 실행되지 않는다. 서버에 최초 한 번 설치해야 한다.

## 보관과 실패 처리

- `private/notion-token`: 소유 사용자 전용 권한 600. 백업 폴더는 권한 700이다.
- `data/YYYY-MM-DD/`: 완료 덤프와 슬롯별 JSON 메타데이터. 종료 코드·mysqldump 완료 표식·gzip CRC·크기·SHA-256 확인 후 원자적으로 확정한다.
- 네트워크 요청 전에 로컬 덤프를 생성한다. Notion 장애 시 로컬 완료 파일은 남고 매 5분 재전송한다. 파일명+SHA-256 표식을 조회해 응답이 끊긴 첨부의 중복을 피한다.
- 파일명·실제 시작/완료 시각·해시는 Notion 페이지 본문에도 남긴다. 각 시간별 파일과 최종 파일은 별도 파일 속성에 모은다. `검증=복원 확인`은 해당 날짜의 모든 보관 파일을 실제 복원 검사한 경우에만 표시한다.
- 미완료 업로드 세션은 만료 후 다시 전송한다. 무료/유료 워크스페이스 파일 크기 한도를 봇 응답으로 확인하며 초과를 성공으로 처리하지 않는다. gzip은 암호화가 아니므로 백업 페이지 공유 범위를 제한한다.
- 기본값으로 백업을 자동 삭제하지 않는다. 디스크 여유가 512MiB 또는 예상 덤프 용량의 두 배보다 작으면 중단한다. 보관 용량·업로드 실패를 감시해야 한다.
- 수집 실패는 systemd가 5분 간격으로 제한 재시도한다. 원인 해결 뒤 해당 시간 안에 `hourly` 또는 19:30 이후 `dump`를 재실행할 수 있다. 이미 지나간 스냅샷은 복원할 수 없으며 업로드 재시도가 새 수집을 대신하지 않는다.
- EC2 전체가 멈추면 Notion에 실패를 기록할 수 없다. 외부 누락 감시는 별도 구성해야 한다.

```bash
journalctl -u 'gujeuk-backup@*' -n 50 --no-pager
systemctl --failed
sudo systemctl start gujeuk-backup@hourly.service
sudo systemctl start gujeuk-backup@retry.service
```

## 검증

```bash
cd ops/gujeuk/backup
python3 -m unittest -v test_backup.py
python3 test_restore.py
```

단위 테스트는 동일 슬롯 재사용·실패 파일 거부·날짜 경계·시간 제한·누락 표시·Notion 장애 중 로컬 보존·중복 첨부 방지를 확인한다. 실제 Docker 테스트는 같은 인스턴스에 별도 DB와 표식을 만든 뒤 대상 DB의 시간별·최종 덤프만 독립 복원하고 다른 DB가 포함되지 않는지 확인한다. 이는 운영 데이터의 실제 복원 검증을 대신하지 않는다.
