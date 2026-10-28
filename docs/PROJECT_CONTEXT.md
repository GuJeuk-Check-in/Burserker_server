# GuJeuk 프로젝트 현재 상황

## 2026-10-26 #142 시설 예약 API 작업 (로컬 검증, 미배포)

- 구즉 이용자 화면에서 기존 `POST /user`로 회원 존재 여부를 확인하고 기존 회원은 `POST /user/reservation`으로 예약만 저장합니다. 새 회원은 `POST /user/sign-up`에 선택적 `reservationTime`을 보내면 회원 생성 → 체크인 기록 저장 → 예약 저장 순서로 처리합니다. 예약 시간이 없으면 기존 가입·체크인만 처리하며, 예약 여부와 무관하게 `visitTime`은 필수입니다. 전체 가입 처리는 하나의 트랜잭션으로 예약 실패 시 회원 생성과 체크인도 함께 롤백됩니다. 별도 예약 API에서는 체크인 기록과 방문 횟수를 변경하지 않습니다. 이번 작업에는 예약 가능 시간 및 예약 가능한 방문 목적을 조회하는 API나 목적 목록 응답 변경을 포함하지 않습니다.
- 방문 목적의 `reservation_status`가 `true`일 때만 예약할 수 있습니다. 기존 목적의 기본값은 `false`이고 관리자가 목적 생성·수정 시 이 값을 지정할 수 있습니다.
- `POST /user/reservation`은 로그인 토큰 없이 호출하는 공개 API입니다. 컨트롤러에서는 기관 ID를 받지 않고, 예약 서비스가 기존 회원가입과 동일하게 구즉 기관 ID 1을 사용합니다. 예약 경로의 별도 ORGAN 인증 규칙은 제거하여 `/user/**` 공개 규칙을 적용합니다.
- 예약은 한국 시간 기준 당일 날짜와 선택한 09:30~20:30 슬롯으로 저장하며 각 슬롯은 1시간입니다. 서버에서 지난 시각 여부는 별도 검사하지 않습니다. 같은 회원의 여러 예약은 허용하지만 같은 목적·날짜·시각은 DB 유니크 제약으로 한 건만 허용합니다.
- 예약 API는 구즉 기관에서 요청 전화번호가 없으면 이름만으로, 전화번호가 있으면 이름을 비교하지 않고 전화번호만으로 `User`를 단건 조회합니다. 입력값과 DB 값 모두 하이픈을 제거하여 비교하며, 예약 서비스에서 회원 존재 여부를 재검사하지 않습니다. 동일 조건의 회원이 여러 명이면 JPA 단건 조회 오류가 발생합니다. 예약에는 확인된 회원 이름과 DB의 원본 전화번호를 저장하고 대표 전화번호는 없으면 NULL로 둡니다. 회원 조회 응답의 회전식 `userId`는 예약 대상 특정에 사용하지 않습니다.
- V14/V15 마이그레이션으로 목적 예약 여부 및 예약 테이블을 추가하고, V16으로 예약 이름 저장과 선택적 전화번호를 적용합니다. 로컬 MySQL 8 격리 컨테이너에서 V14/V15의 기존 목적 기본값과 중복 슬롯 차단을 확인했습니다. 운영 DB에는 적용하지 않았습니다.
- 예약 종료 후 안내 문자 발송 서비스는 아직 정해지지 않아 **발송 기능은 미구현**입니다. 대표 전화번호가 없는 예약에는 발송할 수 없으므로 발송 서비스·재시도·발송 상태와 번호 없는 예약의 처리 정책은 별도 구현이 필요합니다.
- 요청에 따라 이번 예약 작업에서 작성한 구즉 테스트 소스 5개를 제거했습니다. 기존 법동 연결 테스트는 유지합니다. 사용하지 않는 예약 검증 예외도 정리했고 `./gradlew --offline --no-daemon clean build`가 통과했습니다. 구즉 테스트는 소스가 없어 실행되지 않습니다.

## 2026-10-21 #135 최종 환경별 Compose와 Redis 공유 (로컬 구성·미배포)

- 최종 요구는 기존 gujeuk-aws-prod/stag 프로젝트에 각각 app/mysql/redis + beopdong-app/beopdong-mysql 다섯 서비스를 두는 것입니다. 기관별 ops 폴더는 유지하고 법동 파일을 기존 구즉 Compose에 합칩니다. 이전 독립 beopdong 프로젝트 및 스테이징만 배포한다는 설명은 더 이상 적용하지 않습니다.
- 법동 workflow는 develop→stag(8082), main→prod(8083)에 배포합니다. 서버 디렉터리는 기존 `/home/ubuntu/gujeuk-aws/{stag,prod}`를 사용합니다. 기존 .env와 별도로 .env.beopdong을 준비하며 법동 전용 변수는 BEOPDONG_ 접두어를 사용하며, JWT 키는 JWT_SECRET_KEY를 사용합니다. 법동 MySQL은 기관·환경별 전용 볼륨을 사용하고 구즉의 기존 세 서비스와 볼륨 설정은 보존합니다.
- Redis는 환경별 기존 구즉 컨테이너를 공유합니다. 구즉 LettuceConnectionFactory는 기존 DB 0을 유지하고 법동은 Spring Data Redis 설정으로 DB 1을 사용합니다. 논리 DB는 보안·자원·장애 격리가 아니며 FLUSHALL을 사용하지 않습니다. 법동은 Redis에 Refresh Token을 저장합니다.
- 법동 배포는 서비스 이름을 지정해 두 서비스만 관리하고 기존 법동 MySQL은 --no-recreate로 유지합니다. 구즉 배포도 app과 기존 의존 서비스만 관리하며 법동 서비스는 건드리지 않습니다. 전체 down/--remove-orphans/볼륨 삭제는 자동 배포에 넣지 않습니다. 같은 호스트의 prod/stag 기동은 공용 flock으로 직렬화합니다.
- 첫 법동 환경 기동은 가용 RAM 1536MiB 및 디스크 2GiB 기준입니다. 기존 약 4GiB EC2에 두 환경 추가 시 부족 가능성이 크므로 실제 자원 확인·증설을 먼저 진행해야 합니다. swap을 RAM으로 계산하지 않습니다. 실제 서버 SSH 조회는 시간 초과, AWS CLI 로그인은 만료되어 현 상태는 미확인입니다.
- 검증: Java 17 `./gradlew --offline --no-daemon clean build` 통과(일반 실행에서 DB/Redis 외부 연결 테스트는 opt-in으로 건너뜀). 운영/스테이징 merged config가 각각 5개 서비스이며 기존 구즉 세 서비스와 볼륨의 렌더링 결과가 동일함을 확인했습니다.
- 임시 컨테이너로 두 환경 각각 5개 서비스를 기동했습니다. 이 인프라 검증의 구즉 App은 경량 stand-in이며 구즉 업무 API 검증은 아닙니다. 법동 추가 전후 기존 구즉 서비스 ID와 Redis DB 0 테스트 키 유지, 법동 전용 MySQL SELECT·Hikari/Spring 기동·HTTP 404·앱→Redis PING·법동 MySQL 재생성 방지를 확인했습니다. 임시 자원만 정리했습니다.
- 실제 법동 Spring RedisConnectionFactory의 PING 및 동일 키 DB 0/1 분리 테스트 2개를 통과했습니다. UUID 테스트 키만 사용하고 제거했습니다. 추출한 실제 SSH script에 대해 두 환경 배포 대상, env 누락, 자원 부족, 포트·볼륨 오설정, Redis 없음, 이미지 다운로드 실패, DB/앱/Redis 연결 실패를 검증하는 격리 테스트 7개를 통과했습니다.
- 기존 ops/monitoring의 Prometheus/Grafana 설정 삭제도 별도 정리 대상입니다. 저장소 파일 삭제가 EC2의 기존 모니터링 컨테이너를 중지하는 작업은 아닙니다. Datadog 전환 이력과 분리해 해석합니다.
- 실제 서버 배포·도메인·Caddy 변경·푸시·머지는 하지 않습니다. Caddy/Cloudflare 및 법동 APM은 별도 작업입니다. 아래 과거 설명보다 이 절과 ops/beopdong/README.md를 우선합니다.

## 2026-10-21 #135 구즉 ops 디렉터리명 명시

- 기존 AWS 배포 디렉터리를 `ops/gujeuk/`으로 이동했습니다. 구즉 workflow의 SCP source와 관련 실행 안내·문서 경로를 함께 갱신했습니다. 기존 Caddy·EC2 초기화 설정도 이 폴더 안에 함께 유지합니다.
- 저장소 안의 경로만 변경했습니다. EC2의 `/home/ubuntu/gujeuk-aws/{prod,stag,proxy}` 경로, 컨테이너·네트워크·DB 볼륨 이름, 포트와 배포 명령은 그대로입니다. 서버 적용·푸시·머지는 하지 않았습니다.

## 2026-10-21 #135 기관별 workflow 파일명 명시

- 구즉은 `.github/workflows/gujeuk-ci-cd.yml`, 법동은 `.github/workflows/beopdong-ci-cd.yml`로 이름을 통일했습니다. 법동 workflow 자체 경로 필터와 구즉의 법동 workflow 제외 경로, 관련 문서를 함께 수정했습니다. job·명령·배포 조건은 변경하지 않았으며 푸시·배포는 하지 않았습니다.

## 2026-10-21 #135 법동 배포 형식을 구즉과 통일 (미푸시·EC2 미적용)

- 법동도 `ops/beopdong/docker-compose.aws.yml`, `stag.env.example`을 사용하고 서버에서는 `docker-compose.yml`로 복사합니다. 서비스명은 구즉과 같은 app/mysql, 변수는 APP_IMAGE·APP_CONTAINER_NAME·APP_PORT·DD_ENV·MYSQL_*·DB_URL·JAVA_OPTS로 통일했습니다. 이전 초안의 BEOPDONG_* 변수와 docker-compose.stag.yml은 더 이상 사용하지 않습니다.
- 구즉 workflow의 verify → package-image → deploy job 및 step 이름·artifact 전달·SCP·SSH script 구조를 따릅니다. 별도 deploy-stag.sh와 그 전용 Python 테스트는 제거했습니다. PR은 법동 컴파일, main/수동은 JAR 빌드, develop push만 이미지 게시·EC2 배포를 수행합니다.
- Dockerfile은 구즉처럼 ARG JAR_FILE과 COPY를 사용합니다. 법동 미사용 Redis·JWT·CORS·Datadog Java tracer는 추가하지 않습니다. 업무 Java·DB 마이그레이션 내용은 변경하지 않습니다.
- 법동 스테이징 컨테이너·프로젝트·DB·볼륨·8082 포트 값은 이전 초안에서 유지합니다. 구즉의 실제 Compose·workflow와 개인 env는 변경하지 않습니다. 메모리 상한, 최초 가용 RAM 1536MiB/디스크 2GiB 검사, 구즉 env 오복사 방지, 기존 MySQL --no-recreate를 유지합니다.
- MySQL healthcheck는 구즉과 동일한 mysqladmin 방식이며, 배포 script에서 앱 계정 SELECT 1을 별도 확인합니다. 앱은 API가 없어 Hikari/Spring 기동 로그와 루트 HTTP 404/200을 검사합니다. 구즉 stag와 같은 Actions concurrency 그룹을 사용합니다. Caddy·Cloudflare·법동 APM 연결은 별도입니다.
- 변경된 Dockerfile 이미지 빌드, Compose의 필수 env 11개 누락 시 실패, 서비스 app/mysql·메모리 상한·DB 포트 비공개·법동 볼륨을 검증했습니다. workflow에서 추출한 SSH script에 대한 격리 검사 7개(최초/재배포, 자원·포트·볼륨 오설정, env 누락, 이미지 pull 실패, DB 실패, 앱 실패)를 통과했습니다.
- 임시 이름·포트·볼륨으로 실제 App·MySQL을 기동하여 mysql 호스트명 DB 연결·앱 계정 SELECT·Hikari/Spring 기동·HTTP 404·DB 컨테이너 ID 유지를 확인했습니다. 테스트 자원만 정리했습니다. 구즉 실제 파일과 개인 env에는 diff가 없습니다.
- 실제 서버 배포·푸시·PR 생성은 하지 않았습니다. 아래의 이전 초안 파일명·전용 스크립트·테스트 설명보다 이 절과 `ops/beopdong/README.md`를 우선합니다.

## 2026-10-21 #135 법동 스테이징 자동 배포 설정 (미푸시·EC2 미적용)

- 법동 workflow를 CI/CD로 확장했습니다. 대상 경로의 develop push에서만 JAR 검증 → 별도 GHCR `beopdong-api:stag-<SHA>` 게시 → EC2 배포를 수행합니다. main·PR·workflow_dispatch는 검증만 하며 법동 운영 배포는 없습니다.
- `ops/beopdong/docker-compose.stag.yml`은 독립 프로젝트 `beopdong-aws-stag`로 App·MySQL 2개만 정의합니다. 호스트 8082, DB `beopdong_stag`, 전용 볼륨 `beopdong-stag-mysql-data`, 서버 디렉터리 `/home/ubuntu/beopdong-aws/stag`입니다. 기존 구즉 Compose·볼륨·네트워크는 수정하지 않습니다.
- 서버 .env를 최초 머지 전에 권한 600으로 준비해야 합니다. 값은 저장소에서 복사하지 않습니다. 기존 `EC2_HOST`·`EC2_USER`·`EC2_SSH_KEY`와 GHCR 패키지 생성/pull 권한이 필요합니다.
- App 메모리 상한 512MiB/JVM heap 256MiB, MySQL 상한 768MiB입니다. 배포 전 새로 기동할 서비스의 상한 + 256MiB 가용 RAM 및 디스크 2GiB를 요구합니다. 첫 기동은 MemAvailable 1536MiB 기준이며 swap은 제외합니다. 기준은 보수적인 시작값이고 부하에 대한 안전 보장은 아닙니다. 서버의 현재 자원은 이번 작업에서 확인하지 않았습니다.
- 배포는 법동 전용 서비스만 지정합니다. 기존 MySQL은 `--no-recreate`로 보존하고 App만 교체합니다. 구즉 stag deploy와 같은 Actions concurrency 그룹을 사용합니다. 수동 구즉 배포는 이 직렬화 대상이 아닙니다.
- DB healthcheck는 앱 계정의 SELECT 1입니다. 업무 API/헬스체크가 없어 앱 검사에는 Hikari 풀·Spring 기동 로그와 루트 HTTP 404/200을 사용합니다. 이는 업무 readiness 또는 외부 HTTPS 확인이 아닙니다. 앱 단일 교체로 짧은 중단이 있을 수 있고 자동 롤백은 없습니다.
- 현재 라벨은 법동 service/env를 식별하지만 Java tracer는 추가하지 않아 법동 APM 수집이 자동 연결되지는 않습니다. Caddy·Cloudflare도 자동 변경하지 않습니다. 법동 도메인의 중첩 서브도메인 인증서 범위를 확인한 뒤 기존 경로를 보존하며 별도로 연결합니다.
- 가짜 Docker 기반 배포 안전 조건 테스트 7개를 통과했습니다. 실제 Compose 렌더링에서 서비스 2개·메모리 제한·DB 포트 비공개·필수 env 누락 시 실패를 검증했습니다. 임의 이름/임시 포트/전용 테스트 볼륨으로 App·MySQL을 기동하여 앱 계정 SELECT·Hikari 풀·HTTP 404를 확인했고, DB 재배포 시 동일 컨테이너 ID 유지도 확인했습니다. 검증 자원만 정리했습니다.
- EC2 읽기 조회를 시도했으나 SSH 22번 포트 연결이 시간 초과되어 현재 RAM·디스크·env 존재 여부는 확인하지 못했습니다. 실제 서버 env 작성·컨테이너 기동·도메인 연결·푸시·PR 생성은 하지 않았습니다. 아래의 이전 빌드 전용 설명보다 이 절과 `ops/beopdong/README.md`를 우선합니다.

## 2026-10-21 #135 모노레포와 법동 로컬 DB 구성 (로컬 검증, 미배포)

- 루트 Gradle은 `apps:gujeuk-api`, `apps:beopdong-api`, `shared:common`을 관리합니다. Java 17·Spring Boot 3.5.6을 유지합니다.
- 기존 구즉 코드를 `apps/gujeuk-api/src/`로 이동하고 사용자가 진행한 `com.example.burserker.gujeuk` 패키지 리팩터링을 유지합니다. 구즉 업무 로직·application.yml·마이그레이션 내용은 이번 모듈/DB 구성 작업에서 수정하지 않았습니다. QueryDSL 생성 파일은 컴파일 결과입니다.
- 법동은 `BeopdongApplication`으로 독립 실행하며 JPA·MySQL 드라이버·DataSource 설정을 추가했습니다. JWT 인증을 제공하며, RefreshToken은 @RedisHash("RefreshToken") 모델로 Redis에 저장합니다. 헬스체크·마이그레이션은 없습니다. JWT 키는 JWT_SECRET_KEY를 사용합니다. 공통 모듈도 빈 상태이고 앱 간 의존성은 없습니다.
- 실제 로컬 설정은 저장소 밖 `~/env/법동_local.env`에 권한 600으로 보관합니다. 구즉 로컬 MySQL 계정을 사용하되 대상 스키마만 `BeopDong`으로 분리합니다. 비밀값은 추적하지 않고 공개 양식만 커밋합니다.
- 법동 IntelliJ 비공개 실행 설정 `Beopdong - Local DB`를 추가했습니다. `.idea`와 개인 env는 Git 제외 대상입니다. 로컬 포트는 8082이며 검증용 실행은 별도 임시 포트로 기동 후 종료했습니다.
- 법동 `ddl-auto: none`으로 자동 DDL을 막습니다. 구즉 Flyway SQL은 법동 모듈의 클래스패스에 포함하지 않습니다. 로컬 MySQL의 lower_case_table_names=2에서 접속 DB 이름은 소문자로 반환되지만 SCHEMA_NAME은 `BeopDong`이며 테스트는 이를 고려합니다.
- 법동 실제 앱 DataSource를 사용하는 로컬 연결/SELECT 테스트 2개를 통과했습니다. 이 테스트는 `BEOPDONG_LOCAL_DB_TEST=true`일 때만 실행되어 일반 CI에는 개인 DB가 필요하지 않습니다. env 없는 CI 방식에서는 테스트 2개 SKIPPED와 빌드 성공을 확인했습니다.
- Java 17 전체 clean build, 법동 실행 JAR의 DB 풀 기동/HTTP 404, Docker 이미지 빌드 및 임시 컨테이너의 호스트 DB 연결/HTTP 404를 확인했습니다. Docker 검증에서만 호스트를 `host.docker.internal`로 덮어썼으며 개인 env는 localhost로 유지했습니다. BeopDong 테이블 수는 검증 전후 0개로 동일합니다.
- 구즉 JAR은 `apps/gujeuk-api/build/libs/gujeuk-api.jar`, 법동 JAR은 `apps/beopdong-api/build/libs/beopdong-api.jar`입니다. 각 앱 Dockerfile은 모듈 안에 있으며 저장소 루트 컨텍스트에서 빌드합니다.
- 기존 구즉 workflow의 Gradle·artifact·Dockerfile 경로를 새 구조에 맞췄습니다. 법동 전용 경로만 바뀌면 구즉 push 배포를 제외합니다. 법동 workflow는 빌드 검증 전용이며 자동 배포는 없습니다.
- 목표는 실제 EC2 스테이징에 법동 App·전용 MySQL 2개만 추가하는 것입니다. 운영 법동과 법동 Redis는 만들지 않습니다. `ops/gujeuk/`의 기존 Compose·서버 디렉터리·포트·볼륨·이미지 경로를 이번 작업에서 변경하지 않았습니다.
- 아래 과거 기록의 루트 src/Dockerfile/산출물 경로보다 이 절의 경로를 우선합니다. 로컬 검증은 EC2 배포 완료를 의미하지 않으며 푸시·PR 생성은 하지 않습니다.

## 2026-10-07 develop → main 통합 검토 (PR 준비, 운영 상태 미확인)

- 비교 기준은 main `e9250bb`와 develop `3d9cbe0`이다. 2026-09-07부터 2026-10-07까지 두 브랜치에 반영된 커밋 이력을 검토했다.
- main에 이미 반영된 리팩터링·Datadog·로그 설정을 유지하며 develop의 이름 검색(`/organ/search`), 12개월 방문기록 건수(`/log/count/{year}`), localhost 5175/5176 CORS 허용을 통합한다.
- 두 신규 조회 API는 기존 develop 구현대로 전체 기록을 조회한다. 이 통합에서 기관별 조회 조건을 추가하지 않는다.
- 기존 Caddy 별칭 제거와 Log 복합 unique 선언 제거도 develop 이력을 유지한다. Caddy는 앱 CI/CD의 자동 적용 대상이 아니며, 운영 DB 인덱스 상태는 이번 작업에서 확인하지 않았다.
- Java 파일 4개와 문서 2개의 병합 충돌을 해결했다. Java 17에서 `./gradlew --offline --no-daemon clean build`와 diff 공백 검사를 통과했다. 테스트 소스가 없어 기능 테스트는 실행되지 않았다.
- 소스 충돌 해결과 로컬 검증은 운영 배포 완료를 의미하지 않는다. 아래의 날짜별 운영 확인 기록과 구분한다.

## 2026-10-06 main 대상 모니터링 및 리팩터링 변경 (PR 준비, 미배포)

- Dockerfile에 Datadog Java tracer를 추가하고 루트 및 AWS 배포 Compose의 앱·MySQL·Redis에 service/env 라벨을 추가하고 앱에 Agent 연결 환경변수를 지정한다. CI가 main은 `DD_ENV=prod`, develop은 `DD_ENV=stag`로 전달한다.
- 앱은 각 환경 Docker 네트워크의 `datadog-agent:8126`으로 trace를 보낸다. 실제 Agent 연결 및 APM 수집 검증은 배포 전후에 필요하다.
- 운영 `.env`와 서버 Compose에는 VERCEL_URL이 없지만 기존 운영 컨테이너에는 예전 값이 남아 있음을 2026-10-06 SSH 읽기 조회로 확인했다. 새 기동 시 누락을 방지하기 위해 앱 CORS 설정, Compose, 환경 예시, 동기화 스크립트에서 해당 항목을 함께 제거한다. 운영 프론트 주소는 PROD_BASE_URL로 허용한다.
- #115 및 #120의 리팩터링 68개 커밋을 원래 메시지·작성자·작성 시각을 유지하여 반영한다. 엑셀 인증, 일부 API 삭제, 요청 검증 변경이 포함된다. JWT access 만료는 기존 하드코딩 7200초에서 설정값 7200초로 전환되어 기본 유효시간은 같다. 최종 DB 마이그레이션 diff는 없다. #127과 동일하게 기존 monitor-bot 소스를 삭제한다. 소스 삭제가 EC2의 기존 컨테이너를 자동으로 중지하거나 삭제하지는 않는다.
- MySQL/Redis에도 라벨 변경을 적용한다. 기존 배포 명령은 전체 `up -d`이므로 DB·Redis 컨테이너도 재생성될 수 있다. 볼륨 이름은 유지하지만 일시적인 연결 단절 가능성을 배포 전에 확인한다. 앱 배포는 단일 컨테이너 교체이며 무중단이 아니다.
- 수동 Compose 실행에는 DD_ENV=prod/stag 지정이 필요하다. tracer는 기존 develop과 같은 latest 다운로드 방식으로, 버전 고정은 후속 검토 사항이다.
- 후속 로그 hotfix(54800b6, 2026-09-18)를 원래 메시지·작성자 그대로 추가했다. application.yml은 develop과 동일하게 root: INFO이며 Redis/Lettuce DEBUG 설정은 제거됐다. AWS Compose는 Hibernate SQL·바인딩 및 Redis 관련 로그를 warn으로 덮어쓴다.
- 2026-10-06 통합 후 clean build는 통과했으나 테스트 소스는 원본 #115에서 삭제되어 자동 기능 테스트는 실행되지 않았다. 로컬 FE 소스에서 회원 엑셀에 인증 인터셉터를 사용하는 것은 확인했다. 배포된 FE와의 일치 및 실제 API 호환성은 확인하지 않았다.
- 이번 운영 영향 확인 중 EC2 SSH 연결이 타임아웃되어 최신 메모리·컨테이너 상태를 확인하지 못했다. 운영 무중단 및 수집 정상 여부는 보장하지 않는다.
- 아래 과거 운영 기록보다 이 절의 변경 범위와 확인 시점을 우선한다. PR 작성만으로 배포 완료나 로그 수집 정상 여부를 의미하지 않는다.



> 최종 갱신 및 EC2 확인: 2026-09-22 15:33 KST
> 확인한 작업 브랜치: `chore/#119-ChoreUnuseDelete` (`322b00d` 기준)
> 목적: 현재 코드·운영 구성·배포 시 주의할 차이를 파악하기 위한 기준 문서

## 1. 먼저 확인할 현재 상태

- 현재 운영 대상은 **AWS EC2 `3.37.79.125`**다. 체크인 운영·스테이징, 게임, 모니터링이 같은 EC2에서 실행된다.
- 홈서버, Oracle HAProxy, Cloudflare Tunnel, Primary/Replica 승격 절차는 과거 구성이다. 현재 AWS 운영 절차로 사용하지 않는다. 과거 기록은 이 파일의 Git 이력에서 확인한다.
- `develop` push는 EC2 스테이징, `main` push는 EC2 운영으로 자동 배포된다. PR은 Java 컴파일만 수행한다.
- 저장소의 최신 코드, EC2에 저장된 파일, 실행 중 컨테이너의 이미지·환경변수는 서로 다를 수 있다. 아래의 **구성 차이**를 먼저 읽는다.
- 아래 정보는 확인 시점의 스냅샷이다. 배포·DB·네트워크 작업 전에 실제 상태를 다시 확인한다.

## 2. 서비스와 코드

시설 출입 관리용 Spring Boot 백엔드다. 운영자별로 이용자·방문 목적·거주지·출입 기록을 관리한다.

| 항목 | 현재 코드 기준 |
|---|---|
| 기술 | Java 17, Spring Boot 3.5.6, Gradle, MySQL 8, Redis 7.2 |
| 주요 구성 | Spring Security/JWT, JPA, QueryDSL, Flyway, Apache POI |
| 패키지 | `src/main/java/com/example/gujeuck_server` |
| 도메인 | `organ`, `user`, `purpose`, `residence`, `log`, `common` |
| 공통 기능 | readiness 확인, 체크인 퍼널 이벤트 기록·조회 |
| 게임 | 별도 `GuJeuk-Game-FE` 저장소의 `server/`에서 관리 |

주요 API:

- `/organ/**`: 기관 로그인, 토큰 재발급, 회원 관리, Excel, 방문 통계·이용 현황.
- `/user/**`: 이용자 등록·체크인 및 간편 등록·출입 기록.
- `/purpose/**`, `/residence/**`: 방문 목적·거주지 관리와 순서 변경.
- `/log/**`: 방문 기록 생성·조회·수정·삭제.
- `/common/analytics/check-in-funnel`: 퍼널 이벤트 등록. `/events`는 이벤트 조회. 현재 `SecurityConfig`에서 `ORGAN` 역할을 요구한다.
- `GET /common/health/ready`: DB 연결 유효성 검사. 정상은 200, 실패는 503. Redis나 모든 업무 기능의 정상 여부를 보장하는 검사는 아니다.

인증 여부는 `global/config/SecurityConfig.java`를 기준으로 판단한다. `/user/**`, `/organ/create`, `/organ/login`, `/purpose/all`, `/residence/all`, `/common/health/ready` 등은 공개 경로다. `/organ/excel/user`는 현재 코드에서 인증이 필요하다. 모든 API가 로그인 제외 전부 인증 필요하다고 단정하지 않는다.

API의 최종 기준은 컨트롤러·DTO·SecurityConfig다. `apiDocument.md` 등 설명 문서와 차이가 있으면 코드를 확인한다. 예전 `/public/organs`를 배포 health check로 사용하지 않는다.

## 3. 접속 주소와 경로

| 용도 | 주소/경로 |
|---|---|
| 운영 API | `https://aws-api.oijwef098234.com` |
| 스테이징 API | `https://aws-stag.oijwef098234.com` |
| 게임 API | `https://game-api.oijwef098234.com` |
| 운영 프론트 | `https://gujeuk-check-in-fe.pages.dev` |
| 스테이징 프론트 | `https://gujeuk-check-in-develop.pages.dev` |
| 관리자 화면 | 운영 프론트의 `/organ/login` |
| 이용자 화면 | 운영 프론트의 `/check-in` |
| SSH | `ssh ubuntu@3.37.79.125` |
| 체크인 운영 | `/home/ubuntu/gujeuk-aws/prod` |
| 체크인 스테이징 | `/home/ubuntu/gujeuk-aws/stag` |
| Caddy | `/home/ubuntu/gujeuk-aws/proxy` |
| 게임 | `/home/ubuntu/gujuck-game` |
| 모니터링 | `/home/ubuntu/monitoring` |

현재 호스트명은 `ip-172-31-2-113`이다. SSH 직접 접속을 확인했다. 프론트 주소는 현재 사용하는 주소이며, 이번 확인에서 브라우저의 모든 화면을 테스트한 것은 아니다.

추가 접속 참고 문서는 [AWS_EC2_ACCESS.md](AWS_EC2_ACCESS.md)다. 해당 문서의 IAM·SSM·보안 그룹 설명은 과거 확인 기록도 포함하므로 현재 권한과 네트워크 상태를 확인한다. SSH가 안 된다는 이유만으로 다른 관리자의 보안 그룹 허용 규칙을 지우지 않는다.

## 4. EC2 컨테이너·네트워크·저장소

확인 시 실행 중인 컨테이너는 11개다.

| 컨테이너 | 역할 | 호스트 → 컨테이너 포트 | Docker 네트워크 |
|---|---|---|---|
| `gujeuk-aws-caddy` | HTTPS 및 도메인별 프록시 | 80:80, 443:443 TCP/UDP | `proxy_default` |
| `gujeuk-app-prod` | 체크인 운영 | 8080:8080 | `gujeuk-aws-prod_default` |
| `gujeuk-mysql-prod` | 운영 MySQL 8 | 호스트 게시 없음, 내부 3306 | 운영 네트워크 |
| `gujeuk-redis-prod` | 운영 Redis 7.2 | 호스트 게시 없음, 내부 6379 | 운영 네트워크 |
| `gujeuk-app-stag` | 체크인 스테이징 | 8081:8080 | `gujeuk-aws-stag_default` |
| `gujeuk-mysql-stag` | 스테이징 MySQL 8 | 호스트 게시 없음, 내부 3306 | 스테이징 네트워크 |
| `gujeuk-redis-stag` | 스테이징 Redis 7.2 | 호스트 게시 없음, 내부 6379 | 스테이징 네트워크 |
| `gujuck-game-app` | 게임 서버 | 8095:8090 | 운영 네트워크 |
| `monitor-bot` | 상태 확인·지표 제공·Discord 연동 | 127.0.0.1:8090 → 8090 | `monitoring` + 운영 네트워크 |
| `prometheus` | 지표 수집 | 127.0.0.1:9090 → 9090 | `monitoring` |
| `grafana` | 대시보드·경보 | 127.0.0.1:3000 → 3000 | `monitoring` |

데이터 연결:

- 운영 앱: `mysql:3306/gujeuk_prod`, `redis:6379`.
- 스테이징 앱: `mysql:3306/gujeuk_stag`, `redis:6379`.
- 두 환경의 `mysql`, `redis`는 이름이 같아도 네트워크가 달라 각각의 컨테이너에 연결된다.
- 게임 앱: 운영 MySQL 컨테이너 안의 별도 DB `gujuck_game`. 체크인 운영 MySQL을 중지·삭제하면 게임에도 영향이 있다.
- 운영 볼륨: `gujeuk-aws-prod-mysql-data`, `gujeuk-aws-prod-redis-data`.
- 스테이징 볼륨: `gujeuk-aws-stag-mysql-data`, `gujeuk-aws-stag-redis-data`.
- Caddy 볼륨: `gujeuk-aws-caddy-data`, `gujeuk-aws-caddy-config`.
- 모니터링 볼륨: `monitoring_prometheus_data`, `monitoring_grafana_data`.

Compose 프로젝트/볼륨 이름을 임의로 변경하면 기존 DB 대신 새 빈 볼륨으로 기동될 수 있다. 앱 포트 8080·8081·8095는 전체 인터페이스에 게시되어 있으나 외부 직접 접근 가능 여부는 보안 그룹·방화벽 확인이 별도로 필요하다.

## 5. Cloudflare와 Caddy

공개 API DNS 및 응답 헤더에서 Cloudflare 경유를 확인했다. EC2의 Caddy는 도메인에 따라 호스트 포트로 전달한다. 운영·스테이징 사이의 부하 분산이나 자동 장애 전환 구성이 아니다.

| 요청 도메인 | Caddy upstream |
|---|---|
| `aws-api.oijwef098234.com` | `host.docker.internal:8080` |
| `aws-stag.oijwef098234.com` | `host.docker.internal:8081` |
| `game-api.oijwef098234.com` | `host.docker.internal:8095` |

- 저장소: `ops/gujeuk/Caddyfile`, `ops/gujeuk/docker-compose.proxy.yml`.
- 서버: `/home/ubuntu/gujeuk-aws/proxy/Caddyfile` → 컨테이너 `/etc/caddy/Caddyfile`에 마운트.
- `host.docker.internal`은 Compose의 `host-gateway` 설정으로 EC2 호스트를 가리킨다.
- Caddy가 원본 HTTPS를 처리한다. Cloudflare 관리 콘솔의 SSL 모드와 원본 연결 프로토콜은 이번에 확인하지 않았다.

**확인된 차이:** 현재 브랜치의 `Caddyfile`에서는 `api.taisu.site`, `api-stag.taisu.site`가 제거됐다. 하지만 EC2 파일과 실행 중인 Caddy 설정에는 두 도메인이 각각 운영·스테이징의 별칭으로 남아 있다. 이는 taisu로 다시 전달하는 설정이 아니다. 체크인 앱 CI/CD는 Caddyfile을 복사하거나 Caddy를 reload하지 않으므로, 앱 배포만으로 이 변경이 반영되지는 않는다.

## 6. CI/CD

### 2026-10-06 저장소 Datadog 배포 설정 변경 (EC2 미배포·미확인)

- 실제 배포 파일 `ops/gujeuk/docker-compose.aws.yml`에 앱 APM 환경변수와 앱·MySQL·Redis의 Datadog service/env 라벨을 추가했다.
- 배포 SSH 스크립트는 `DD_ENV="$DEPLOY_TARGET"`을 export한다. main은 prod, develop은 stag이며, Compose의 앱 환경변수와 세 서비스의 env 라벨이 같은 값을 사용한다.
- 수동 Compose 실행 시에도 `DD_ENV`가 필수다. 환경별 `.env`에 `DD_ENV=prod` 또는 `DD_ENV=stag`를 지정하거나 셸에서 export한다. 예시는 `ops/gujeuk/*.env.example`에 있다.
- 앱의 trace 목적지는 `datadog-agent:8126`이다. 별도로 실행하는 Agent가 각 앱의 Docker 네트워크에 연결되어 해당 이름으로 접근 가능해야 한다. 이번 수정에서는 네트워크·서버 환경파일·컨테이너를 변경하지 않았다.
- Dockerfile에는 Java Agent 로드 설정이 있다. 실제 EC2 네트워크 연결과 APM 수집 여부는 이번 작업에서 확인하지 않았다. 아래 런타임 및 모니터링 현황은 기존 2026-09-22 스냅샷이다.


기준: `.github/workflows/gujeuk-ci-cd.yml`.

| 이벤트 | 수행 작업 |
|---|---|
| `main`/`develop` 대상 PR | GitHub-hosted runner에서 `./gradlew compileJava` |
| `develop` push | JAR·이미지 빌드 → GHCR → EC2 스테이징 배포 |
| `main` push | JAR·이미지 빌드 → GHCR → EC2 운영 배포 |
| `workflow_dispatch` | 현재 조건상 JAR 빌드·artifact 업로드만 수행. 이미지 생성·배포 job은 push만 허용 |

배포 순서:

1. JDK 17로 `./gradlew bootJar -x test` 실행.
2. Dockerfile로 이미지 생성, GHCR에 `prod-${GITHUB_SHA}` 또는 `stag-${GITHUB_SHA}` 태그로 push.
3. SCP로 `ops/gujeuk/docker-compose.aws.yml`을 EC2의 해당 환경 `docker-compose.yml`로 복사.
4. SSH로 배포 디렉터리에 접속. 기존 `.env`를 사용하고 `APP_IMAGE`는 이번 이미지 주소로 export.
5. `COMPOSE_PROJECT_NAME=gujeuk-aws-prod` 또는 `gujeuk-aws-stag` 설정.
6. `docker compose --env-file .env pull app`, 이어서 `docker compose --env-file .env up -d` 실행.
7. 로컬 및 공개 `/purpose/all`의 정상 응답을 각각 최대 40회, 3초 간격으로 확인. 요청 소요 시간은 별도다.
8. 로컬 확인 이후 사용하지 않는 dangling 이미지를 정리한다.

주의:

- 서버에서 앱 이미지를 빌드하거나 홈서버 self-hosted runner로 배포하는 구조가 아니다.
- `up -d`는 앱만 지정하지 않으므로 Compose 변경에 따라 다른 서비스도 영향을 받을 수 있다.
- 단일 앱 컨테이너 교체 방식이며, 무중단 배포나 자동 롤백은 구성되어 있지 않다.
- `.env`를 저장소에서 복사하는 단계는 없다. 서버 파일과 실행 컨테이너 환경변수를 구분한다.
- Caddy·게임·모니터링은 이 앱 배포 workflow로 자동 배포되지 않는다. `ops/gujeuk/docker-compose.aws.yml`만 복사한다.
- push에서 `docs/**`, `**/*.md`, `monitor-bot/**`, `ops/monitoring/**`, `.github/ISSUE_TEMPLATE/**`만 바뀐 경우 workflow가 제외된다. PR은 이 경로 제외 조건을 적용하지 않는다.
- 현재 CI에 테스트 실행 단계와 Discord 배포 알림 단계는 없다. `src/test` 디렉터리도 현재 체크아웃에 없다.

## 7. 실행 이미지와 현재 코드의 차이

2026-09-22 15:33 KST 확인:

| 환경 | 실행 이미지 |
|---|---|
| 운영 | `ghcr.io/gujeuk-check-in/gujeuk-check-in-server:prod-bf732f25c2560c29d4b6ba50b4ecdd0e3ede87dd` |
| 스테이징 | `ghcr.io/gujeuk-check-in/gujeuk-check-in-server:stag-05ee8eb555f0551621f7f453d5decb82917faede` |
| 게임 | `ghcr.io/gujeuk-check-in/gujeuk-game-fe-server:ad07088` |

운영과 스테이징의 이미지 버전은 다르다. 현재 작업 브랜치에는 #113 리팩터링, #117 Caddy 변경, #119 미사용 구성 정리 이력이 있지만 이를 모두 운영 반영 완료로 설명하지 않는다. 현재 브랜치의 #119 변경에는 `.square` 관련 구성 삭제가 포함된다.

## 8. 환경변수와 CORS

### 2026-10-07 로컬 프론트 CORS 허용 추가 (코드 변경, 미배포)

- `SecurityConfig`에서 기존 환경변수 기반 허용 목록에 `http://localhost:5175`, `http://localhost:5176`을 추가한다.
- 두 주소는 `TEST_URL` 값과 별개로 허용된다. 다른 호스트·포트에 대한 와일드카드는 추가하지 않는다.
- 서버 환경변수와 실행 컨테이너는 변경하지 않았다. 해당 코드가 배포된 환경부터 적용된다.

현재 코드의 CORS 변수는 다음 세 개다. 여러 origin은 쉼표로 구분하며 코드가 공백과 후행 `/`를 정리한다.

```dotenv
PROD_BASE_URL=https://gujeuk-check-in-fe.pages.dev
STAG_BASE_URL=https://gujeuk-check-in-develop.pages.dev
TEST_URL=http://localhost:5173,http://localhost:5174
```

- EC2 prod/stag `.env`는 위 주소로 정리돼 있고 `VERCEL_URL`은 없다. prod `TEST_URL`에는 쉼표 뒤 공백이 있으며 현재 코드에서는 trim 처리된다.
- 실행 중인 stag 컨테이너는 정리된 세 변수를 사용하고 `VERCEL_URL`이 없다.
- **실행 중인 prod 컨테이너에는 아직 예전 CORS 주소 목록과 `VERCEL_URL`이 남아 있다.** 이전 이미지로 기동한 컨테이너의 환경변수는 `.env` 수정만으로 바뀌지 않는다.
- 현재 코드와 정리된 환경변수를 함께 적용해야 한다. 이전 이미지가 요구하는 변수를 없앤 상태로 이전 이미지만 재기동하지 않는다.

앱 실행에는 DB 연결 정보, JWT secret, Redis 연결 정보가 필요하다. AWS Compose에는 이미지·컨테이너 이름·호스트 포트·볼륨 이름도 필요하다. `ops/gujeuk/prod.env.example`, `stag.env.example`은 형식 참고용이며 실제 `.env`가 아니다.

`${VAR:?message}`는 필수값이 없거나 비어 있으면 Compose 실행을 실패시키는 문법이다. `APP_IMAGE`는 배포 쉘에서 전달할 수 있으므로 서버 `.env`만으로 실행 설정을 판단하지 않는다.

## 9. 모니터링

- 현재 구성은 `monitor-bot`, Prometheus, Grafana다. 과거 Loki·Alloy·node_exporter·cAdvisor 기반 홈서버 구성을 현재 EC2 구성으로 설명하지 않는다.
- monitor-bot은 Docker 소켓을 마운트하며 운영 네트워크와 모니터링 네트워크에 연결된다.
- 현재 소스의 기본 감시 대상은 운영 앱·MySQL·Redis와 Caddy다. 스테이징은 감시 대상에서 제외돼 있다.
- EC2 monitor-bot 환경변수의 운영 health URL은 `http://gujeuk-app-prod:8080/common/health/ready`다.
- Prometheus는 `monitor-bot:8090/actuator/prometheus`와 자신의 지표를 15초마다 수집한다.
- Grafana 데이터 소스는 `http://prometheus:9090`이다. Discord contact point와 경보 규칙이 있다.
- monitor-bot의 Discord 토큰/Webhook 설정 존재를 확인했으며 실제 알림 발송 테스트는 하지 않았다.
- Prometheus의 `alertmanagers`는 빈 목록이다. 별도 Alertmanager 컨테이너는 실행 중이지 않다.
- 이 구성은 운영과 같은 EC2에 있으므로 EC2 전체 장애 시 모니터링도 영향을 받는다.
- 호스트의 3000·8090·9090은 loopback에만 바인딩돼 있다. 필요하면 SSH 포워딩으로 접근한다.

```bash
ssh -N -L 13000:127.0.0.1:3000 ubuntu@3.37.79.125
# 로컬 브라우저에서 http://localhost:13000
```

## 10. DB와 배포 안전 기준

- 현재 `application.yml`은 Flyway 활성화, `baseline-version: 6`, `validate-on-migrate: false`, Hibernate `ddl-auto: update`를 함께 사용한다. 앱 기동이 DB 변경을 일으킬 수 있다.
- 마이그레이션 파일은 V1~V13이다. V13은 과거 체크인 DB의 펫 관련 테이블을 삭제하는 SQL이다. 현재 게임 DB 공유 구조와 혼동하지 말고 실행 대상 DB와 적용 이력을 확인한다.
- 과거 문서의 V14 시간 변환/backfill 파일은 현재 브랜치에 없다.
- 이번 갱신에서는 DB 내용·건수·Flyway 적용 이력·복제 상태를 조회하지 않았다. 과거 dump 복원 건수는 현재 수치로 사용하지 않는다.
- DB 변경·복원 전에 대상 환경·DB·볼륨을 확인하고 백업한다. 복원 파이프에서는 `docker compose exec -T`를 사용한다.
- 운영 데이터 삭제는 조건과 건수를 먼저 확인하고 사용자 승인 범위를 따른다. `docker compose down -v`처럼 볼륨을 삭제하는 명령을 정리 작업에 사용하지 않는다.
- Caddy는 운영·스테이징·게임이 함께 사용한다. 한 서비스 작업 때문에 공통 프록시를 임의로 중지하지 않는다.
- 홈서버의 Wi-Fi 변경, RTC 절전, DB 승격, cloudflared 재시작 명령을 AWS에 적용하지 않는다. `AGENTS.md`의 관련 장비별 지침은 과거 홈서버 작업 시의 안전 기록이다.
- `.env`, 토큰, 비밀번호, SSH 키, Webhook, DB dump를 출력·문서화·커밋하지 않는다. 컨테이너 전체 Env 대신 필요한 비민감 항목만 확인한다.
- 현재 `git ls-files backups` 결과는 비어 있고 `.gitignore`가 `backups/`, `*.env`, dump를 제외한다. 예전 문서의 “백업 secret 파일이 현재 추적 중”이라는 설명은 현재 체크아웃에 해당하지 않는다. 과거 Git 이력 정리와 자격증명 교체 완료 여부는 별도 확인이 필요하다.

## 11. 실제 확인 결과와 자주 쓰는 조회 명령

2026-09-22 15:33 KST에 다음 8개 GET 요청은 모두 HTTP 200이었다.

| 대상 | `/purpose/all` | `/common/health/ready` |
|---|---|---|
| EC2 `localhost:8080` | 200 | 200 |
| EC2 `localhost:8081` | 200 | 200 |
| `https://aws-api.oijwef098234.com` | 200 | 200 |
| `https://aws-stag.oijwef098234.com` | 200 | 200 |

이 결과는 전체 업무 기능, CORS, 인증, Excel, 게임의 통합 테스트를 의미하지 않는다. MySQL/Redis 컨테이너 4개는 Docker 상태에서 healthy였다.

```bash
# 로컬에서 실행
ssh ubuntu@3.37.79.125
curl -s -o /dev/null -w '%{http_code}\n' https://aws-api.oijwef098234.com/common/health/ready
curl -s -o /dev/null -w '%{http_code}\n' https://aws-stag.oijwef098234.com/common/health/ready

# EC2에서 실행: 상태와 실제 이미지 확인
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
docker inspect gujeuk-app-prod --format '{{.Config.Image}}'
docker inspect gujeuk-app-stag --format '{{.Config.Image}}'
```

로그가 필요하면 `docker logs --tail 100 <컨테이너명>`으로 범위를 제한하고, 개인정보·자격증명을 대화나 문서로 옮기지 않는다. Compose 명령은 해당 환경 디렉터리 및 배포 시 사용한 이미지·프로젝트 변수를 확인한 뒤 사용한다.

## 12. 후속 작업과 갱신 규칙

확인된 차이를 해소할 때 별도로 검토할 사항:

1. 운영 이미지와 현재 코드/CORS 환경변수의 차이를 운영 배포 전에 검토한다.
2. 저장소의 Caddy 도메인 제거를 서버에 반영할지는 기존 도메인 사용 여부를 확인하고 결정한다. 앱 배포가 Caddy를 갱신한다고 가정하지 않는다.
3. 실제 정기 백업, 외부 저장, 복구 검증 및 외부 장애 감시 상태는 별도 확인한다. 구성돼 있다고 단정하지 않는다.
4. 과거 자격증명 노출 이력에 대한 정리·교체 여부는 별도 확인한다.

인프라·URL·Compose·CI/CD·환경변수·DB 변경 시 이 문서를 갱신한다. **확인 날짜, 현재 코드, 실제 런타임, 미확인 사항을 구분**하고 과거 작업 이력을 현재 상태 아래 누적하지 않는다. 문서만 수정한 작업에서는 앱·DB·프록시를 재시작하거나 배포하지 않는다.
