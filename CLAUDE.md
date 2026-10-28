# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드 작업 시 참고하는 가이드를 제공합니다.

## 프로젝트 개요

GuJeuk-Check-In Server는 시설 출입 관리를 위한 Spring Boot 백엔드입니다. 사용자 등록, 출입 로그 기록, 관리자 운영을 처리하며 멀티 테넌트를 지원합니다 (각 관리자가 자신의 시설을 관리).

## 빌드 및 실행 명령어

```bash
# 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun

# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew :apps:gujeuk-api:test --tests "TestClassName"
./gradlew :apps:gujeuk-api:test --tests "com.example.burserker.gujeuk.domain.admin.service.LoginServiceTest"

# QueryDSL Q-classes 생성 (컴파일 시 자동 생성됨)
./gradlew compileJava

# JAR 빌드
./gradlew bootJar
```

## 필수 환경 변수

```
DB_URL=jdbc:mysql://host:port/dbname
DB_NAME=database_username
DB_PASSWORD=password
JWT_SECRET_KEY=your_secret_key
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 아키텍처

### 기술 스택
- Java 17, Spring Boot 3.5.6
- MySQL 8 with Spring Data JPA
- QueryDSL 5.0.0 (복잡한 쿼리용)
- JWT 인증 (Redis 토큰 저장소)
- Apache POI (Excel 내보내기)

### 도메인 주도 패키지 구조

```
apps/gujeuk-api/src/main/java/com/example/burserker/gujeuk/
├── domain/
│   ├── admin/          # 관리자 계정, JWT 인증, Excel 내보내기
│   ├── user/           # 사용자 등록, 프로필
│   ├── purpose/        # 시설 목적/유형
│   └── log/            # 출입 기록
├── global/
│   ├── config/         # Security, JPA, QueryDSL 설정
│   ├── security/jwt/   # JwtTokenProvider, JwtTokenFilter
│   ├── entity/         # BaseIdEntity (공통 ID 필드)
│   └── error/          # 전역 예외 처리
└── infrastructure/
    └── excel/          # Excel 생성 유틸리티
```

### 도메인 모듈 패턴

각 도메인은 다음 구조를 따릅니다:
- `domain/` - JPA 엔티티와 enums
- `domain/repository/` - Spring Data repositories + QueryDSL 커스텀 저장소
- `service/` - 단일 목적 서비스 (예: CreateAdminService, LoginAdminService)
- `facade/` - 도메인 간 조정
- `presentation/` - 컨트롤러와 DTOs
- `exception/` - 도메인별 예외

### 주요 엔티티 및 관계

- **Admin** - 시설 소유자. 다수의 Users, Purposes, Logs를 가짐.
- **User** - 등록된 방문자. 하나의 Admin에 속함.
- **Purpose** - 시설 유형/카테고리. 하나의 Admin에 속함.
- **Log** - 출입 기록. User(익명 방문의 경우 선택적)와 Admin을 참조.

### QueryDSL

Q-classes는 `apps/gujeuk-api/src/main/generated/`에 생성됩니다. 커스텀 저장소 구현체는 복잡한 쿼리를 위해 QueryDSL을 사용합니다:
- `UserRepositoryCustom` / `UserRepositoryCustomImpl`
- `LogRepositoryCustom` / `LogRepositoryCustomImpl`
- `PurposeRepositoryCustom` / `PurposeRepositoryCustomImpl`

## API 엔드포인트

- `/admin/**` - 관리자 로그인, 비밀번호 변경, 사용자 관리, Excel 내보내기
- `/user/**` - 사용자 회원가입, 로그인
- `/purpose/**` - 시설 목적 CRUD
- `/log/**` - 출입 로그 CRUD

모든 엔드포인트는 JWT Bearer 토큰 인증을 사용합니다 (회원가입/로그인 제외).

## 코드 컨벤션

- 서비스는 작업별로 명명: `CreateXxxService`, `QueryXxxService`, `UpdateXxxService`, `DeleteXxxService`
- Facades는 도메인 간 로직을 조정
- DTOs는 `presentation/dto/request/`와 `presentation/dto/response/`의 record 타입
- 예외는 도메인별 기본 예외를 상속
- BaseIdEntity는 `@MappedSuperclass`를 통해 공통 `@Id` 필드를 제공
## 기관별 모듈 경계 (#135)

- `apps/gujeuk-api`: 기존 구즉 앱. Java·SQL 내용은 유지하고 경로만 이동했습니다.
- `apps/beopdong-api`: 법동 독립 실행 앱과 MySQL 연결 설정. 기관 로그인 API·JWT 인증·Redis 기반 Refresh Token 저장을 제공합니다.
- `shared/common`: 빈 공통 라이브러리. 기관별 앱을 의존하지 않으며 두 앱도 아직 연결하지 않았습니다.
- 구즉 실행 클래스는 `apps/gujeuk-api/src/main/java/com/example/burserker/GuJeuckServerApplication.java`입니다. 두 앱은 서로의 런타임 클래스패스를 공유하지 않습니다.
- 구즉 빌드: `./gradlew :apps:gujeuk-api:bootJar`; 산출물: `apps/gujeuk-api/build/libs/gujeuk-api.jar`.
- Docker 빌드: 저장소 루트에서 `docker build -f apps/gujeuk-api/Dockerfile -t gujeuk-check-in-server:local .`.
- 법동 CI는 빌드만 검증합니다. 구즉 배포 대상·포트·볼륨·서버 설정은 유지합니다.

### 법동 로컬 DB 검증

- 개인 local env는 저장소 밖 `~/env/법동_local.env`이며 내용과 실제 자격증명을 출력·커밋하지 않습니다. 공개 양식은 `apps/beopdong-api/.env.local.example`입니다.
- 대상은 로컬 `BeopDong` 스키마입니다. `ddl-auto: none`을 유지하며 구즉 마이그레이션을 연결하지 않습니다.
- `BEOPDONG_LOCAL_DB_TEST=true`와 법동 local env를 함께 제공할 때만 실제 DB 테스트 2개를 실행합니다. 일반 CI는 이 테스트를 건너뜁니다.
- Docker Desktop에서 호스트 MySQL을 테스트할 때만 DB_URL 호스트를 `host.docker.internal`로 덮어씁니다. IDE env의 localhost 값을 바꾸지 않습니다.
- 법동 배포 범위는 향후 스테이징 App·MySQL 2개이며 아직 EC2 배포 설정은 추가하지 않았습니다.
