# 구즉 운영 DB → Notion 자동 백업

EC2가 구즉 `gujeuk_prod`만 전체 덤프하여 Notion 날짜별 페이지에 첨부한다. Python 표준 라이브러리와 Docker를 사용하며 노트북·SSH 연결 유지가 필요 없다.

## 실행 흐름

`systemd timer → service → backup.py → mysqldump → EC2 파일 → Notion API`

| 한국 시간 | 호출 | 작업 |
| --- | --- | --- |
| 00:00 | `backup.py page` | 날짜별 페이지와 본문 양식 생성 |
| 01:00~19:00 매 정각 | `backup.py hourly` | 해당 시각의 전체 덤프 생성·첨부 |
| 19:30 | `backup.py dump` | 최종 전체 덤프 생성·첨부 |
| 매 5분 | `backup.py retry` | 기존 파일 업로드 재시도. 새 덤프는 생성하지 않음 |

`.timer`의 `OnCalendar`가 실행 시각을 정하고 `.service`의 `ExecStart`가 Python을 실행한다. `systemctl enable --now`로 타이머를 켜면 재부팅 후에도 예약이 유지된다. 놓친 과거 시점의 DB 상태를 소급 생성하지 않는다.

## 파일과 페이지

| 파일 | 역할 |
| --- | --- |
| `backup.py` | 일정별 작업, 로컬 파일, 날짜별 페이지 양식과 파일 첨부 |
| `backup_core.py` | MySQL 덤프·압축, Notion HTTP 요청 |
| `configure.py` | Notion 토큰 숨김 입력과 연결 확인 |
| `install.sh` | 프로그램 복사와 systemd 타이머 설치 |
| `systemd/` | 실행 명령과 예약 시각 |

서버 저장 위치는 `/home/ubuntu/gujeuk-notion-backup/data/YYYY-MM-DD/`다. `mysqldump --single-transaction --databases gujeuk_prod`로 SQL을 추출하고 `.sql.gz`로 압축한다. 완료된 파일과 JSON 기록을 남긴 뒤 Notion API에 전송한다. 업로드가 실패하면 파일은 EC2에 남고 다음 재시도에서 사용한다. 운영 데이터·다른 DB는 변경하지 않는다.

페이지 제목은 `YYYY.MM.DD / [PROD]Gujeuk_DB`다. 본문 양식은 `page_template()`에서 생성하며, 최종 파일은 ‘전체 DUMP 파일’ 아래에, 매시간 파일은 ‘시간대별 DUMP 파일 → HH:00’ 아래에 첨부한다. 오류는 본문 마지막 ‘오류 내용’에 기록한다. Notion의 수동 템플릿 버튼에 의존하지 않는다.

DB 속성은 이름·백업일·환경·마지막 백업 시각·검증·시간별 덤프 파일·최종 덤프 파일을 유지한다. 상태·시간별 덤프 수·최종 덤프 시각·오류 내용 속성은 사용하지 않는다.

## 설치

Notion 내부 연결에 백업 상위 페이지 접근 권한을 주고 검토한 코드를 EC2에 복사한다. 토큰과 실제 설정은 Git에 넣지 않는다.

```bash
bash ops/gujeuk/backup/install.sh --prepare
python3 /home/ubuntu/gujeuk-notion-backup/configure.py --database-id <백업_DB_UUID>
cd /home/ubuntu/gujeuk-notion-backup
python3 backup.py bootstrap
bash install.sh --enable
systemctl list-timers 'gujeuk-backup-*' --no-pager
```

`install.sh`도 대상 디렉터리에 복사해서 실행한다. 기존 설치 업데이트는 코드만 교체하고 `config.json`, `private/notion-token`, `data/`는 유지한다. DB·페이지 확인 중에는 타이머를 잠시 멈추고 진행 중인 백업과 배포가 끝난 후 교체한다. 머지만으로 서버 코드가 바뀌지는 않는다.

## 확인과 한계

```bash
journalctl -u 'gujeuk-backup@*' -n 30 --no-pager
python3 -m unittest -v test_backup.py
python3 test_restore.py
```

완료 파일 재사용·실패 파일 제외·로컬 보존·첨부 중복 방지를 유지한다. 실제 복원 테스트는 일회용 MySQL만 사용한다. 운영 서버에 자동 복원하거나 매시간 복원 검증용 컨테이너를 띄우지 않는다.

Notion 워크스페이스의 파일 크기 한도를 넘으면 업로드 오류를 남기고 EC2 파일을 보존한다. 분할 업로드·binlog·자동 삭제·외부 장애 알림은 구현하지 않는다. 토큰은 권한 600, 백업 디렉터리는 700으로 보관하며 gzip은 암호화가 아니다.

전체 덤프 하나로 해당 시점의 상태를 복구한다. 마지막 백업 이후 변경은 포함되지 않으며 19:30부터 다음 날 01:00까지는 5시간 30분 공백이 있다.
