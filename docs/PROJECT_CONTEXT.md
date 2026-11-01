# 구즉·법동 프로젝트 현황

> 기준일: 2026-10-29. 저장소 설정과 실제 서버 상태를 구분합니다.
> 과거 변경 이력은 Git에서 확인하고, 이 문서에는 현재 구조와 작업에 필요한 내용만 유지합니다.

## 1. 먼저 확인할 사항

- 현재 작업은 `bug/#148-beopdong-CI/CD-bug`의 법동 배포 오류 수정입니다. 서버에 직접 적용하지 않았으며 `develop` 머지 후 자동 배포 결과를 확인해야 합니다.
- 2026-10-29 Datadog 법동 스테이징 로그에서 JWT 키 길이 검사 실패와 앱 종료·재기동 반복을 확인했습니다. EC2 전체 CPU 사용량을 모두 법동 때문이라고 단정하지 않습니다.
- 최근 EC2 SSH 접속은 시간 초과되었습니다. 서버 env, 재시작 횟수, 현재 운영 이미지·메모리·디스크 상태는 재확인이 필요합니다.
- API·인증의 기준은 컨트롤러·DTO·SecurityConfig입니다. 설명 문서나 과거 배포 결과만으로 현재 상태를 판단하지 않습니다.

## 2. 모노레포 구조

| 경로 | 역할 |
|---|---|
| `apps/gujeuk-api` | 구즉 체크인·회원·시설·방문 기록·예약·통계·Excel API |
| `apps/beopdong-api` | 법동 독립 Spring Boot 앱, 기관 인증·비밀번호 변경·DB/Redis 연결 |
| `shared/common` | 공통 라이브러리 자리. 현재 앱 간 공유 의존성 없음 |
| `ops/gujeuk` | 구즉 Compose, env 예시, Caddy 구성 |
| `ops/beopdong` | 법동 추가 Compose, 운영·스테이징 env 예시 |
| `.github/workflows` | 기관별 CI/CD |

- 공통: Java 17, Spring Boot 3.5.6, Gradle, MySQL 8, Redis 7.2, Spring Security/JWT, JPA.
- 구즉 추가 구성: QueryDSL, Flyway, Apache POI, Datadog Java tracer.
- Java 패키지: `com.example.burserker.gujeuk`, `com.example.burserker.beopdong`.
- 법동은 구즉 클래스·Flyway SQL을 공유하지 않으며 `ddl-auto: none`을 사용합니다.

```bash
./gradlew :apps:gujeuk-api:bootJar
./gradlew :apps:beopdong-api:bootJar
./gradlew clean build
```

산출물은 각 모듈의 `build/libs/gujeuk-api.jar`, `build/libs/beopdong-api.jar`입니다. Docker 빌드 컨텍스트는 저장소 루트이며 Dockerfile은 각 모듈에 있습니다. 루트 `bootJar`·`bootRun`은 구즉 실행의 편의 명령입니다.

## 3. 배포 구성

환경별 하나의 Compose 프로젝트에서 구즉 세 서비스와 법동 두 서비스를 실행하도록 구성합니다.

| 항목 | 운영 | 스테이징 |
|---|---|---|
| 배포 브랜치 | `main` | `develop` |
| Compose 프로젝트 | `gujeuk-aws-prod` | `gujeuk-aws-stag` |
| EC2 경로 | `/home/ubuntu/gujeuk-aws/prod` | `/home/ubuntu/gujeuk-aws/stag` |
| 구즉 앱 포트 | 8080 → 8080 | 8081 → 8080 |
| 법동 앱 포트 | 8083 → 8080 | 8082 → 8080 |
| 구즉 MySQL DB | `gujeuk_prod` | `gujeuk_stag` |
| 법동 MySQL DB | `beopdong_prod` | `beopdong_stag` |

- 서비스명: `app`, `mysql`, `redis`, `beopdong-app`, `beopdong-mysql`.
- 구즉 파일은 서버 `docker-compose.yml`, 법동 파일은 `docker-compose.beopdong.yml`로 복사합니다.
- 구즉 배포는 `app`과 해당 의존 서비스만, 법동 배포는 법동 App·MySQL만 관리합니다. 법동 MySQL은 `--no-recreate`로 유지합니다.
- 같은 EC2의 배포는 `/home/ubuntu/gujeuk-aws/.deploy.lock`의 flock으로 직렬화합니다. 앱 단일 교체 방식이며 무중단 배포·자동 롤백은 없습니다.
- 법동 App 상한은 512MiB, JVM heap 기본값은 256MiB, MySQL 상한은 768MiB입니다. 최초 두 서비스 기동 시 가용 RAM 1536MiB와 디스크 2GiB를 요구하며 swap은 가용 RAM으로 계산하지 않습니다.
- 상세 절차는 [법동 배포 안내](../ops/beopdong/README.md)를 확인합니다.

### 데이터 분리

- 구즉 MySQL 볼륨: `gujeuk-aws-{prod,stag}-mysql-data`.
- 법동 MySQL 볼륨: `beopdong-{prod,stag}-mysql-data`.
- Redis 볼륨: `gujeuk-aws-{prod,stag}-redis-data`.
- 각 환경의 기존 Redis를 구즉 DB 0, 법동 DB 1로 공유합니다. 운영·스테이징 Redis는 별개입니다. 논리 DB는 보안·자원·장애 격리가 아니므로 `FLUSHALL`을 사용하지 않습니다.
- 게임은 과거 확인 기준으로 운영 MySQL의 별도 `gujuck_game` DB를 사용합니다. 운영 MySQL 작업 전 게임 영향도 확인합니다.

## 4. 환경변수와 JWT

| 구분 | 보관 위치·전달 방식 |
|---|---|
| 구즉 서버 env | 배포 디렉터리의 `.env` |
| 법동 서버 env | 같은 디렉터리의 `.env.beopdong` |
| 법동 서버 JWT | `BEOPDONG_JWT_SECRET_KEY` → 컨테이너 `JWT_SECRET_KEY` |
| 법동 로컬 env | `~/env/법동_local.env`, 앱 변수는 `JWT_SECRET_KEY` |
| 공개 예시 | `ops/{gujeuk,beopdong}/*.env.example`, `apps/beopdong-api/.env.local.example` |

- 법동 JWT 키는 UTF-8 기준 64바이트 이상의 무작위 값이어야 합니다. 서버 env에 적는 것 외에 Compose의 `environment` 전달 설정도 필요합니다.
- 배포 시 기존 `.env`와 `.env.beopdong`을 함께 읽습니다. 법동 전용 변수에는 `BEOPDONG_` 접두어를 사용해 구즉 변수와 충돌하지 않게 합니다.
- 법동 workflow는 Compose 렌더링 후 JWT 키의 공백 여부와 UTF-8 바이트 길이를 검사하고, 공백만 있거나 64바이트 미만이면 앱 교체 전에 중단합니다. 키 자체를 trim하거나 출력하지 않습니다. 예시의 `change-me-` 값도 배포 전에 거부합니다.
- `.env` 수정은 실행 중 컨테이너에 자동 반영되지 않습니다. 앱 재생성이 필요하며, 기존 MySQL 계정 비밀번호 변경은 별도 작업입니다.
- 기존 실패 작업의 Re-run은 이전 커밋의 파일을 사용합니다. 수정한 Compose·workflow를 적용하려면 수정 커밋으로 배포합니다.
- 구즉 CORS는 `PROD_BASE_URL`, `STAG_BASE_URL`, `TEST_URL`과 로컬 5175/5176 설정을 사용합니다. 법동 CORS는 현재 `http://localhost:5175`만 허용합니다. 실제 브라우저 origin 허용 여부는 앱별 SecurityConfig를 확인합니다.

## 5. CI/CD와 기동 확인

| 항목 | 구즉 | 법동 |
|---|---|---|
| workflow | `.github/workflows/gujeuk-ci-cd.yml` | `.github/workflows/beopdong-ci-cd.yml` |
| PR | Java 컴파일 | Java 컴파일 |
| develop/main push | JAR·이미지 빌드 → GHCR → stag/prod | JAR·이미지 빌드 → GHCR → stag/prod |
| 수동 실행 | 빌드, 배포 없음 | 빌드, 배포 없음 |
| 내부 기동 확인 | 공개 `/purpose/all` | 컨테이너 실행 + Hikari/Spring 기동 로그 + Redis PING |
| 외부 HTTPS 확인 | 공개 도메인의 `/purpose/all` 200 | 현재 workflow에 없음 |

- workflow의 경로 필터에 따라 기관별 실행 여부가 결정됩니다. 공통 빌드·모듈 변경은 두 workflow에 영향을 줄 수 있습니다.
- 법동은 루트 HTTP 응답 검사를 하지 않습니다. 별도 헬스체크 컨트롤러·정적 기본 페이지·루트 공개 설정은 추가하지 않습니다.
- 법동 배포는 DB 계정 SELECT, 컨테이너 실행 여부, Hikari/Spring 기동 로그, 앱에서 Redis PING으로 확인합니다. 실제 HTTP 요청 처리·외부 HTTPS는 검사 범위 밖입니다.
- 구즉에는 DB 연결 유효성을 검사하는 공개 `/common/health/ready`도 있지만 현재 CI/CD 검사 경로는 `/purpose/all`입니다.
- env·이미지·서비스 이름 검증을 유지하고 전체 `down`, `--remove-orphans`, 볼륨 삭제를 배포에 추가하지 않습니다.

## 6. 주소·프록시·관측

| 용도 | 주소·경로 |
|---|---|
| SSH | `ssh ubuntu@3.37.79.125` |
| 구즉 운영 API | `https://aws-api.oijwef098234.com` |
| 구즉 스테이징 API | `https://aws-stag.oijwef098234.com` |
| 구즉 프론트 | `https://gujeuk-check-in-fe.pages.dev` |
| 구즉 스테이징 프론트 | `https://gujeuk-check-in-develop.pages.dev` |
| 법동 운영 API 예정 도메인 | `https://aws.beopdong-api.oijwef098234.com` |
| 법동 스테이징 API 예정 도메인 | `https://aws.beopdong-stag-api.oijwef098234.com` |
| 게임 API | `https://game-api.oijwef098234.com` |
| Caddy | `/home/ubuntu/gujeuk-aws/proxy` |

- Caddy는 도메인별로 호스트 앱 포트에 전달합니다. 저장소 구성은 `ops/gujeuk/Caddyfile`, `docker-compose.proxy.yml`에 있으며 앱 CI/CD가 프록시나 DNS를 자동 변경하지 않습니다.
- 과거 서버에는 제거된 `api.taisu.site`, `api-stag.taisu.site` 별칭이 남아 있었습니다. 현재 서버 설정과 법동 도메인 연결 완료 여부는 별도 확인이 필요합니다.
- Datadog 대시보드에서 실제 컨테이너 지표·법동 로그를 확인했습니다. 구즉 이미지에는 Java tracer와 Agent 연결 설정이 있지만 법동 Dockerfile에는 tracer가 없습니다. 컨테이너 지표 수집과 법동 APM 연결을 구분합니다.
- Prometheus/Grafana/monitor-bot은 과거 구성입니다. 관련 파일 정리가 서버 컨테이너 중지·삭제를 의미하지 않으며 현재 실행 여부는 별도 확인합니다.
- 모니터링이 같은 EC2에 있으면 호스트 장애 시 함께 영향을 받습니다. 정기 백업·복구 검증·외부 장애 감시는 구성 완료로 가정하지 않습니다.
- 추가 접속 정보는 [AWS_EC2_ACCESS.md](AWS_EC2_ACCESS.md)를 참고하되 IAM·SSM·네트워크 상태는 재확인합니다.

## 7. 구즉 예약 기능

- 공개 `POST /user/reservation`은 예약만 생성하며 체크인·방문 횟수를 변경하지 않습니다. 기관 ID는 기존 회원가입과 같은 1을 사용합니다.
- 전화번호가 비면 이름으로, 있으면 이름을 비교하지 않고 전화번호로 회원을 단건 조회합니다. 요청·DB 전화번호의 하이픈을 제거해 비교하며 동일 조건의 회원이 여러 명이면 단건 조회 오류가 발생합니다.
- `reservation_status=true`인 방문 목적만 허용합니다. 한국 시간 당일 날짜와 09:30~20:30의 1시간 슬롯을 사용하고 지난 시각 검사는 없습니다. 회원의 여러 예약은 허용하되 같은 목적·날짜·시각은 DB 유니크 제약으로 제한합니다.
- 회원가입에 `reservationTime`이 있으면 회원 생성 → 체크인 → 예약 순서로 같은 트랜잭션에서 처리합니다. 없으면 기존 가입·체크인만 수행하며 `visitTime`은 필수입니다.
- V14~V16은 목적 예약 여부·예약 테이블·이름·선택적 전화번호 변경입니다. 실제 운영 적용 여부는 별도 확인해야 합니다.
- 예약 가능 목적·시간 조회 API와 종료 문자 발송은 구현 범위에 포함되지 않았습니다. 문자 서비스는 미정입니다.

## 8. 데이터·운영 안전 기준

- 구즉은 Flyway와 `ddl-auto: update`를 함께 사용하므로 앱 기동이 DB를 변경할 수 있습니다. 현재 마이그레이션은 V1~V16이며 V13은 과거 펫 테이블 삭제입니다. 적용 대상 DB와 이력을 확인합니다.
- DB 변경·복원 전 대상 환경·DB·볼륨을 확인하고 백업합니다. 복원은 `docker compose exec -T`를 사용합니다. 운영 데이터 삭제는 대상·건수를 조회한 후 명시적 확인을 받습니다.
- Compose 프로젝트·볼륨 이름을 임의 변경하거나 `docker compose down -v`를 정리 명령으로 사용하지 않습니다. Caddy·공유 Redis·운영 MySQL 작업 전 다른 서비스 영향을 확인합니다.
- secret·토큰·비밀번호·SSH 키·Webhook·DB dump를 출력·문서화·커밋하지 않습니다. 로그는 범위를 제한하고 전체 컨테이너 Env는 출력하지 않습니다. 과거 자격증명 노출의 정리·교체 완료 여부는 별도 확인합니다.
- SSH 문제 해결을 위해 다른 관리자의 보안 그룹 규칙을 삭제하지 않습니다. 홈서버 Wi-Fi 변경·RTC 절전·DB 승격·cloudflared 명령을 AWS 운영 절차로 사용하지 않습니다. 장비별 금지사항은 `AGENTS.md`를 따릅니다.
- 로컬 검증 성공과 서버 배포 완료를 구분합니다. 인프라·env·배포·DB 구성을 변경하면 이 문서도 함께 갱신합니다.

## 9. 검증 기록과 남은 작업

- #135: 과거 로컬 격리 환경에서 Compose 결합, 법동 DB/Redis 연결, 구즉 자원 유지 검증을 수행했습니다. 임시 구즉 앱은 경량 stand-in이었으며 업무 API 검증은 아닙니다.
- #142: 과거 로컬 예약 마이그레이션·빌드를 검증했습니다. 요청에 따라 해당 작업의 구즉 테스트 소스는 제거했고 법동 연결 테스트는 유지합니다.
- 법동 연결 테스트는 `BEOPDONG_LOCAL_DB_TEST=true`와 개인 env가 있을 때 실행하는 opt-in 테스트입니다. 일반 CI의 건너뜀을 실제 DB/Redis 연결 성공으로 해석하지 않습니다.
- #148: Java 17 법동 JAR 빌드가 통과했고 산출물에 헬스체크 컨트롤러·정적 기본 페이지가 없음을 확인했습니다. HTTP 검사 없이 컨테이너 실행·Hikari/Spring 로그의 성공·실패 조합 4개를 검증했습니다. 운영·스테이징 Compose의 JWT 분리 전달·짧은/빈 키 거부·구즉 설정 보존 및 workflow YAML/bash 문법 검사도 통과했습니다. 서버 적용은 하지 않았습니다.
- #148 JWT 사전 검사 보완: 운영·스테이징 각각 짧은 키, 빈 키, 공백·탭·줄바꿈·유니코드 공백, 정상 ASCII/UTF-8 키, 앞뒤 공백을 포함한 유효 키의 9개 경우와 Compose를 거친 공백 64개 입력의 거부를 검증했습니다.
- 서버 작업: 각 환경 `.env.beopdong`의 `BEOPDONG_JWT_SECRET_KEY` 준비 → 수정 커밋 배포 → 정상 기동·재시작 중단·CPU 안정 여부 확인. 법동 외부 도메인·APM 연결도 별도 확인합니다.
