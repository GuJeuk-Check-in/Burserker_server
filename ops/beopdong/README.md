# 구즉·법동 운영/스테이징 배포

## 최종 구조

기관별 설정 파일을 합쳐 환경별 하나의 Compose 프로젝트로 실행합니다.

| 환경 | 프로젝트 / EC2 디렉터리 | 기존 서비스 | 추가 서비스 | 법동 포트 |
|---|---|---|---|---|
| 운영 | `gujeuk-aws-prod` / `/home/ubuntu/gujeuk-aws/prod` | `app`, `mysql`, `redis` | `beopdong-app`, `beopdong-mysql` | 8083 |
| 스테이징 | `gujeuk-aws-stag` / `/home/ubuntu/gujeuk-aws/stag` | `app`, `mysql`, `redis` | `beopdong-app`, `beopdong-mysql` | 8082 |

- 기반: `ops/gujeuk/docker-compose.aws.yml` → 서버 `docker-compose.yml`
- 법동 추가: `ops/beopdong/docker-compose.aws.yml` → 서버 `docker-compose.beopdong.yml`
- 기존 구즉 env: 서버 `.env` 유지
- 법동 env: 이 폴더의 `prod.env.example`/`stag.env.example`을 참고해 같은 서버 디렉터리에 `.env.beopdong` 작성

한 Compose에서 이름 충돌을 피하려고 법동 변수에는 `BEOPDONG_` 접두어를 사용합니다. 기존 구즉 컨테이너명·DB·볼륨·Redis 데이터는 유지합니다. 법동 MySQL은 `beopdong-prod-mysql-data` 또는 `beopdong-stag-mysql-data` 전용 볼륨을 사용합니다. DB 포트는 외부에 게시하지 않습니다.

## Redis 공유

환경 안에서 기존 `redis:6379`를 공유합니다. 구즉은 기존 DB 0, 법동은 DB 1입니다. 운영과 스테이징 Redis는 서로 다른 컨테이너입니다. 논리 DB 분리는 키 충돌을 줄일 뿐 메모리·장애·관리 권한을 분리하지 않습니다. 법동 기능 구현 시 키 접두어도 `beopdong:`으로 두고 FLUSHALL을 사용하지 않습니다. 현재 법동에는 Redis 연결 설정만 있고 토큰/캐시 업무 기능은 없습니다.

## 머지 시 동작

- PR: 해당 기관 Java 컴파일 검증
- develop push: 해당 기관 이미지를 `stag-<SHA>`로 게시하고 스테이징 배포
- main push: 해당 기관 이미지를 `prod-<SHA>`로 게시하고 운영 배포
- workflow_dispatch: 빌드만 수행하며 배포하지 않음

구즉 workflow는 기존 Compose로 `app` 및 의존 DB/Redis만 관리하고 법동 서비스에는 명령을 보내지 않습니다. 법동 workflow는 두 Compose를 합치지만 기동/교체 대상을 법동 두 서비스로 한정합니다. 기존 법동 MySQL은 `--no-recreate`로 유지합니다. `--remove-orphans`, 전체 down, 볼륨 삭제는 자동 배포에 사용하지 않습니다. 같은 EC2의 배포 기동은 공용 flock으로 직렬화합니다.

두 파일을 합친 설정은 아래처럼 조회합니다. config 전체에는 비밀번호가 포함되므로 `--services` 또는 `--quiet`를 사용하세요.

```bash
cd /home/ubuntu/gujeuk-aws/stag
export COMPOSE_PROJECT_NAME=gujeuk-aws-stag
export DD_ENV=stag
docker compose --env-file .env --env-file .env.beopdong \
  -f docker-compose.yml -f docker-compose.beopdong.yml config --services
```

`app`, `mysql`, `redis`, `beopdong-app`, `beopdong-mysql` 다섯 개가 나옵니다. 수동으로 전체 up을 실행하기 전 env의 이미지 주소가 현재 배포 버전인지 확인하세요. workflow는 새 이미지 주소를 실행 환경변수로 주입하며 서버 env의 이미지 줄을 자동 갱신하지 않습니다.

## 최초 배포 전 필수 준비

1. 먼저 EC2 메모리 용량을 확보합니다. 법동 App 상한 512MiB/JVM heap 256MiB, MySQL 상한 768MiB이며 환경 하나의 최초 기동에 MemAvailable 1536MiB, 디스크 여유 2GiB를 요구합니다. swap은 RAM으로 계산하지 않습니다. 기존 약 4GiB 서버에 두 환경을 바로 추가하면 부족할 가능성이 높으므로 현재 자원 확인·증설을 먼저 진행합니다. 이 기준 자체가 부하 안정성을 보장하지는 않습니다.
2. 서버 prod/stag 각각 `.env.beopdong`을 작성하고 비밀번호를 서로 다른 실제 값으로 바꾼 뒤 `chmod 600`을 적용합니다. 기존 `.env`는 덮어쓰지 않습니다. private env와 DB dump를 Git·채팅·CI 로그에 넣지 않습니다.
3. 기존 구즉 Compose와 Redis가 먼저 실행 중이어야 합니다. 새 EC2에서는 구즉을 먼저 배포한 뒤 법동 배포를 실행합니다. GitHub의 `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY` 및 GHCR 패키지 생성/pull 권한을 확인합니다.
4. 예전 법동 독립 프로젝트가 실제 기동된 적이 있다면 컨테이너/볼륨 소유 프로젝트 검사에서 중단합니다. 기존 DB를 삭제하거나 자동으로 새 프로젝트에 편입하지 말고, 백업 후 별도 전환 계획을 세웁니다.

서버 `.env.beopdong`에 `BEOPDONG_JWT_SECRET_KEY`를 UTF-8 기준 64바이트 이상의 무작위 값으로 준비합니다. Compose는 이를 법동 앱의 `JWT_SECRET_KEY`로 전달하며 구즉의 JWT 변수와 분리합니다. 기존 실패 작업 Re-run은 이전 커밋의 Compose를 다시 복사하므로 수정 커밋을 배포해야 합니다.

DB 계정 비밀번호는 env 수정만으로 기존 MySQL에 반영되지 않습니다. DB 버전/계정 변경은 별도 작업입니다. 법동은 `ddl-auto: none`을 유지합니다. 별도의 헬스체크 컨트롤러나 정적 기본 페이지는 없으며 루트 경로는 공개하지 않습니다. 배포 검사는 JWT 키가 공백만 있거나 UTF-8 기준 64바이트 미만이면 앱 교체 전에 거부한 뒤 앱 계정 SELECT, 컨테이너 실행 여부, Hikari/Spring 기동 로그, 앱에서 Redis PING을 확인합니다. HTTP 응답과 외부 HTTPS는 검사하지 않습니다. 업무 readiness 검사는 아니며 앱 단일 교체로 짧은 중단이 있고 자동 롤백은 없습니다.

## 도메인·관측

Caddy와 Cloudflare는 앱 workflow에서 자동 변경하지 않습니다. 실제 서버 Caddyfile을 백업한 뒤 법동 도메인 두 개를 각각 host.docker.internal:8083(prod), :8082(stag)로 연결하고 설정 검증·reload·외부 HTTPS 확인을 별도로 진행합니다. 전체 호스트명의 인증서 범위를 확인하고 EC2 앱 포트를 외부에 개방하지 않습니다. 브라우저 업무 API용 CORS·인증도 별도 개발이 필요합니다. Datadog 라벨은 있지만 법동 Java tracer와 Agent 연결은 아직 추가하지 않았습니다.

설정·로컬 검증·커밋은 실제 EC2 배포 성공을 의미하지 않습니다. 최근 EC2 SSH 조회는 시간 초과였으며 AWS CLI 로그인도 만료된 상태였습니다.

## 2026-10-21 로컬 검증

Java 17 전체 clean build, 운영/스테이징 merged config의 5개 서비스와 기존 구즉 설정 보존을 확인했습니다. 두 환경의 임시 컨테이너 기동 및 법동 DB/Redis 연결, 기존 구즉 서비스 ID와 DB 0 테스트 키 보존을 확인했습니다. 구즉 App은 인프라 검증용 stand-in이며 업무 API 테스트는 아닙니다. 법동 RedisConnectionFactory 테스트 2개와 workflow 기동 안전 조건 테스트 7개를 통과했습니다. 테스트 컨테이너·볼륨은 모두 별도 이름으로 만들고 정리했습니다.
