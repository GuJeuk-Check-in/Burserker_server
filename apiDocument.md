# GuJeuk Check-In Server API 명세서

## 목차
1. [User API](#user-api)
2. [Admin API](#admin-api)
3. [Log API](#log-api)
4. [Purpose API](#purpose-api)
5. [Error Code 목록](#error-code-목록)
6. [Enum 정의](#enum-정의)

---

## User API

### 1. 회원가입
| 항목 | 내용 |
|------|------|
| **Domain** | User |
| **기능** | 새로운 사용자 회원가입 |
| **HTTP Method** | `POST` |
| **API Path** | `/user/sign-up` |
| **토큰 필요** | X |

**Request Body**
```json
{
  "name": "string",
  "gender": "MALE|FEMALE",
  "phone": "string",
  "maleCount": 0,
  "femaleCount": 0,
  "birthYMD": "string",
  "residence": "string",
  "privacyAgreed": true,
  "purpose": "string",
  "visitTime": "2026-10-27T10:00:00",
  "reservationTime": "10:30"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | O | 이름 |
| gender | string | O | 성별 (MALE, FEMALE) |
| phone | string | O | 전화번호 |
| maleCount | int | O | 남자 동행인 수 |
| femaleCount | int | O | 여자 동행인 수 |
| birthYMD | string | O | 생년월일 |
| residence | string | O | 거주지 |
| privacyAgreed | boolean | O | 개인정보 동의 |
| purpose | string | O | 방문목적 |
| visitTime | LocalDateTime | O | 기존 회원가입 처리에 필요한 방문시각 |
| reservationTime | string | X | 함께 예약할 시작 시각 (`HH:mm`, 09:30~20:30). 없으면 예약을 생성하지 않습니다. |
| subPurposeId | long | X | 하위 방문목적 단위로 예약할 때 하위 방문목적 ID. `purpose`의 하위여야 합니다. |

회원 생성 → 체크인 기록 저장 → `reservationTime`이 있을 때 예약 저장 순서로 처리합니다. 예약 시간이 있어도 체크인을 함께 저장하므로 `visitTime`은 필수입니다. 예약은 `reservationStatus=RESERVATION`인 방문 목적, 또는 `SUB_INCLUDE` 방문 목적의 `subPurposeReservation=true`인 하위 방문목적(`subPurposeId`)에만 가능하며, 예약 슬롯 충돌 등으로 실패하면 이번 요청의 회원 생성·체크인도 함께 롤백됩니다.

**Response (201 Created)**: 본문 없음

**Error List**
<details>
<summary><code>401</code> USER_EXIST - 유저가 이미 존재합니다</summary>
</details>
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>

---

### 2. 로그인 (체크인)
| 항목 | 내용 |
|------|------|
| **Domain** | User |
| **기능** | 기존 사용자 로그인 (방문 체크인) |
| **HTTP Method** | `POST` |
| **API Path** | `/user/login` |
| **토큰 필요** | X |

**Request Body**
```json
{
  "userId": "string",
  "purpose": "string",
  "maleCount": 0,
  "femaleCount": 0
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| userId | string | O | 사용자 ID |
| purpose | string | O | 방문목적 |
| maleCount | int | O | 남자 동행인 수 |
| femaleCount | int | O | 여자 동행인 수 |

**Response (200 OK)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> USER_NOT_FOUND - 해당 유저가 존재하지 않습니다</summary>
</details>
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>

---

## Admin API

### 1. 관리자 로그인
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 관리자 계정 로그인 |
| **HTTP Method** | `POST` |
| **API Path** | `/admin/login` |
| **토큰 필요** | X |

**Request Body**
```json
{
  "password": "string"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | string | O | 비밀번호 |

**Response (200 OK)**
```json
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | string | JWT Access Token |
| refreshToken | string | JWT Refresh Token |

**Error List**
<details>
<summary><code>404</code> ADMIN_NOT_FOUND - 해당 관리자 계정이 존재하지 않습니다</summary>
</details>
<details>
<summary><code>401</code> PASSWORD_MISMATCH - 비밀번호가 일치하지 않습니다</summary>
</details>

---

### 2. 관리자 생성
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 새로운 관리자 계정 생성 |
| **HTTP Method** | `POST` |
| **API Path** | `/admin/create` |
| **토큰 필요** | X |

**Request Body**
```json
{
  "password": "string"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | string | O | 비밀번호 |

**Response (200 OK)**
```json
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

**Error List**
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>

---

### 3. 비밀번호 변경
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 관리자 비밀번호 변경 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/admin/change` |
| **토큰 필요** | O (Access Token) |

**Request Body**
```json
{
  "oldPassword": "string",
  "newPassword": "string",
  "confirmNewPassword": "string"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| oldPassword | string | O | 기존 비밀번호 |
| newPassword | string | O | 새 비밀번호 |
| confirmNewPassword | string | O | 새 비밀번호 확인 |

**Response (200 OK)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> ADMIN_NOT_FOUND - 해당 관리자 계정이 존재하지 않습니다</summary>
</details>
<details>
<summary><code>401</code> PASSWORD_MISMATCH - 비밀번호가 일치하지 않습니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_PASSWORD_CONFIRM - 비밀번호 확인이 일치하지 않습니다</summary>
</details>
<details>
<summary><code>400</code> SAME_OLD_PASSWORD - 기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다</summary>
</details>

---

### 4. 토큰 갱신
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | Access Token 재발급 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/admin/re-issue` |
| **토큰 필요** | O (Refresh Token in Header) |

**Request Header**
```
Authentication: {refreshToken}
```

**Response (200 OK)**
```json
{
  "accessToken": "string",
  "refreshToken": "string"
}
```

**Error List**
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>
<details>
<summary><code>401</code> EXPIRED_TOKEN - 만료된 토큰입니다</summary>
</details>
<details>
<summary><code>404</code> REFRESH_TOKEN_NOT_FOUND - RefreshToken이 존재하지 않습니다</summary>
</details>

---

### 5. 모든 사용자 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 전체 사용자 목록 조회 (페이지네이션) |
| **HTTP Method** | `GET` |
| **API Path** | `/admin/user/all` |
| **토큰 필요** | O (Access Token) |

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 30 | 페이지 크기 |
| sort | string | id,DESC | 정렬 기준 |

**Response (200 OK)**
```json
{
  "totalCount": 100,
  "slice": {
    "content": [
      {
        "id": 1,
        "name": "string",
        "age": "20대",
        "gender": "MALE",
        "phone": "string",
        "birthYMD": "string",
        "residence": "string",
        "privacyAgreed": true,
        "count": 5
      }
    ],
    "hasNext": true,
    "number": 0,
    "size": 30,
    "numberOfElements": 30
  }
}
```

**Error List**
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>
<details>
<summary><code>403</code> FORBIDDEN - Access Token이 없는 요청입니다</summary>
</details>

---

### 6. 거주지별 사용자 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 거주지 기준 사용자 목록 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/admin/user` |
| **토큰 필요** | O (Access Token) |

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| residence | string | O | 거주지 |
| page | int | X | 페이지 번호 |
| size | int | X | 페이지 크기 |

**Response (200 OK)**
```json
{
  "totalCount": 50,
  "slice": {
    "content": [
      {
        "id": 1,
        "name": "string",
        "age": "20대",
        "gender": "MALE",
        "phone": "string",
        "birthYMD": "string",
        "residence": "string",
        "privacyAgreed": true,
        "count": 5
      }
    ],
    "hasNext": true,
    "number": 0,
    "size": 30,
    "numberOfElements": 30
  }
}
```

**Error List**
<details>
<summary><code>400</code> INVALID_RESIDENCE - 존재하지 않는 거주지 이름입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 7. 사용자 상세 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 특정 사용자 상세 정보 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/admin/user/{id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 사용자 ID |

**Response (200 OK)**
```json
{
  "name": "string",
  "userId": "string",
  "phone": "string",
  "birthYMD": "string",
  "residence": "string"
}
```

**Error List**
<details>
<summary><code>404</code> USER_NOT_FOUND - 해당 유저가 존재하지 않습니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 8. 사용자 정보 수정
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 특정 사용자 정보 수정 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/admin/user/{id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 사용자 ID |

**Request Body**
```json
{
  "name": "string",
  "userId": "string",
  "phone": "string",
  "birthYMD": "string",
  "residence": "string"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | O | 이름 (최대 30자) |
| userId | string | O | 사용자 ID (최대 30자) |
| phone | string | O | 전화번호 |
| birthYMD | string | O | 생년월일 |
| residence | string | O | 거주지 |

**Response (200 OK)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> USER_NOT_FOUND - 해당 유저가 존재하지 않습니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>

---

### 9. 월별 방문 실적 및 누계 통계 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Organ |
| **기능** | 선택한 월의 방문 실적과 해당 연도 누계 이용자 통계 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/organ/statistics/visits?year={year}&month={month}` |
| **토큰 필요** | O (Access Token) |

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| year | int | O | 조회 연도 |
| month | int | O | 조회 월 (1~12) |

**Request**
```json
{
  "query": {
    "year": 2026,
    "month": 4
  },
  "body": null
}
```

**Header**
```json
{
  "Authorization": "Bearer {accessToken}"
}
```

**Response (200 OK)**
```json
{
  "year": 2026,
  "month": 4,
  "cumulative": {
    "total": 6549,
    "youth": {
      "male": 3558,
      "female": 2654,
      "total": 6212,
      "rate": 94.9
    },
    "other": {
      "male": 183,
      "female": 154,
      "total": 337,
      "rate": 5.1
    }
  },
  "monthly": {
    "total": 1760,
    "youth": {
      "male": 902,
      "female": 654,
      "total": 1556,
      "rate": 88.4
    },
    "other": {
      "male": 105,
      "female": 99,
      "total": 204,
      "rate": 11.6
    }
  }
}
```

**산정 기준**
- 이용자 수는 방문 기록의 `maleCount + femaleCount` 합계로 계산합니다.
- 청소년은 `AGE_9_13`, `AGE_14_16`, `AGE_17_19`, `AGE_20_24`입니다.
- 기타는 `BABY`, `ADULT`입니다.
- 누계는 선택 연도의 1월 1일부터 선택 월의 마지막 날까지 계산합니다.
- 이용률은 `분류 이용자 수 / 전체 이용자 수 × 100`이며 소수점 첫째 자리에서 반올림합니다.
- 방문 기록에는 대표 이용자의 연령대만 저장되므로 동행인도 대표 이용자와 같은 분류로 집계합니다.
- 인증된 기관의 방문 기록만 결과에 포함합니다.

**Error List**
<details>
<summary><code>400</code> INVALID_LOG_DATE - 유효하지 않거나 미래인 연월입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 10. 엑셀 파일 다운로드
| 항목 | 내용 |
|------|------|
| **Domain** | Admin |
| **기능** | 월별 방문 로그 엑셀 파일 다운로드 |
| **HTTP Method** | `GET` |
| **API Path** | `/admin/excel/{yearMonth}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| yearMonth | string | 년월 (예: "202501") |

**Response (200 OK)**
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Body: Excel 파일 (byte[])
```

**Error List**
<details>
<summary><code>507</code> EXCEL_GENERATION_FAILED - 엑셀 파일 생성 중 오류가 발생했습니다</summary>
</details>
<details>
<summary><code>400</code> INVALID_DATE - 유효하지 않은 날짜 형식입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

## Log API

### 1. 방문 로그 생성
| 항목 | 내용 |
|------|------|
| **Domain** | Log |
| **기능** | 새로운 방문 로그 생성 |
| **HTTP Method** | `POST` |
| **API Path** | `/log` |
| **토큰 필요** | O (Access Token) |

**Request Body**
```json
{
  "name": "string",
  "age": "TEENS|TWENTIES|THIRTIES|FORTIES|FIFTIES|SIXTIES|SEVENTIES_PLUS",
  "phone": "string",
  "maleCount": 0,
  "femaleCount": 0,
  "purpose": "string",
  "visitDate": "string",
  "privacyAgreed": true,
  "visitTime": "string",
  "residence": "string"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| name | string | O | 이름 (최대 30자) |
| age | string | O | 나이대 (Enum 참조) |
| phone | string | O | 전화번호 (최대 30자) |
| maleCount | int | O | 남자 동행인 수 |
| femaleCount | int | O | 여자 동행인 수 |
| purpose | string | O | 방문목적 |
| visitDate | string | O | 방문 날짜 |
| privacyAgreed | boolean | O | 개인정보 동의 (true여야 함) |
| visitTime | string | O | 방문 시간 |
| residence | string | O | 거주지 |

**Response (201 Created)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>409</code> DUPLICATE_LOG - 이미 해당 시간의 로그가 존재합니다</summary>
</details>
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 2. 방문 로그 수정
| 항목 | 내용 |
|------|------|
| **Domain** | Log |
| **기능** | 기존 방문 로그 수정 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/log/{log-id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| log-id | long | 로그 ID |

**Request Body**
```json
{
  "name": "string",
  "age": "TEENS|TWENTIES|THIRTIES|FORTIES|FIFTIES|SIXTIES|SEVENTIES_PLUS",
  "phone": "string",
  "maleCount": 0,
  "femaleCount": 0,
  "purpose": "string",
  "visitDate": "string",
  "privacyAgreed": true,
  "visitTime": "string",
  "residence": "string"
}
```

**Response (204 No Content)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> LOG_NOT_FOUND - 존재하지 않는 이용목록입니다</summary>
</details>
<details>
<summary><code>409</code> DUPLICATE_LOG - 이미 해당 시간의 로그가 존재합니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 3. 방문 로그 삭제
| 항목 | 내용 |
|------|------|
| **Domain** | Log |
| **기능** | 방문 로그 삭제 |
| **HTTP Method** | `DELETE` |
| **API Path** | `/log/{log-id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| log-id | long | 로그 ID |

**Response (204 No Content)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> LOG_NOT_FOUND - 존재하지 않는 이용목록입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 4. 방문 로그 목록 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Log |
| **기능** | 전체 방문 로그 목록 조회 (페이지네이션) |
| **HTTP Method** | `GET` |
| **API Path** | `/log` |
| **토큰 필요** | O (Access Token) |

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | int | 0 | 페이지 번호 |
| size | int | 30 | 페이지 크기 |
| sort | string | visitDate,id,DESC | 정렬 기준 |

**Response (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "name": "string",
      "maleCount": 0,
      "femaleCount": 0,
      "visitDate": "string"
    }
  ],
  "hasNext": true,
  "number": 0,
  "size": 30,
  "numberOfElements": 30
}
```

**Error List**
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 5. 방문 로그 상세 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Log |
| **기능** | 특정 방문 로그 상세 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/log/{log-id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| log-id | long | 로그 ID |

**Response (200 OK)**
```json
{
  "id": 1,
  "name": "string",
  "age": "TWENTIES",
  "phone": "string",
  "maleCount": 0,
  "femaleCount": 0,
  "purpose": "string",
  "visitDate": "string",
  "privacyAgreed": true
}
```

**Error List**
<details>
<summary><code>404</code> LOG_NOT_FOUND - 존재하지 않는 이용목록입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

## Purpose API

### 1. 방문목적 생성
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 새로운 방문목적 생성 |
| **HTTP Method** | `POST` |
| **API Path** | `/purpose` |
| **토큰 필요** | O (Access Token) |

**Request Body**
```json
{
  "purpose": "string",
  "reservationStatus": "RESERVATION|NONE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| purpose | string | O | 방문목적 (1~30자) |
| reservationStatus | string | O | 예약 상태 (`RESERVATION`: 예약 필요, `NONE`: 예약 불필요). `SUB_INCLUDE`는 하위 방문목적 생성(`POST /purpose/sub`)에서만 설정됩니다 |

**Response (201 Created)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>409</code> PURPOSE_ALREADY_EXIST - 이미 존재하는 방문 목적입니다</summary>
</details>
<details>
<summary><code>400</code> SUB_INCLUDE_NOT_ALLOWED - 하위 방문목적 생성으로만 설정할 수 있는 상태입니다</summary>
</details>
<details>
<summary><code>400</code> BAD_REQUEST - 요청 데이터 검증 실패</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 2. 방문목적 수정
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 기존 방문목적 수정 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/purpose/{id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 방문목적 ID |

**Request Body**
```json
{
  "purpose": "string",
  "reservationStatus": "RESERVATION|NONE|SUB_INCLUDE"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| purpose | string | O | 방문목적 (1~30자) |
| reservationStatus | string | O | 예약 상태 (`RESERVATION`: 예약 필요, `NONE`: 예약 불필요, `SUB_INCLUDE`: 하위 방문 목적 있음) |

- 하위 방문목적이 있는 방문목적은 `reservationStatus`를 `SUB_INCLUDE`로만 보낼 수 있습니다 (이름만 수정 가능).
- 하위 방문목적이 없는 방문목적은 `SUB_INCLUDE`로 바꿀 수 없습니다. `SUB_INCLUDE`는 하위 방문목적 생성(`POST /purpose/sub`)에서만 설정됩니다.
- `RESERVATION`에서 다른 상태로 바꾸면 그 방문목적의 오늘 이후 예약을 삭제합니다. 지난 예약은 남깁니다.

**Response (200 OK)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> PURPOSE_NOT_FOUND - 존재하지 않는 방문목적입니다</summary>
</details>
<details>
<summary><code>409</code> PURPOSE_ALREADY_EXIST - 이미 존재하는 방문 목적입니다</summary>
</details>
<details>
<summary><code>409</code> PURPOSE_STATUS_FIXED - 하위 방문목적이 있는 방문목적은 예약 상태를 변경할 수 없습니다</summary>
</details>
<details>
<summary><code>400</code> SUB_INCLUDE_NOT_ALLOWED - 하위 방문목적 생성으로만 설정할 수 있는 상태입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 3. 방문목적 삭제
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 방문목적 삭제 |
| **HTTP Method** | `DELETE` |
| **API Path** | `/purpose/{id}` |
| **토큰 필요** | O (Access Token) |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 방문목적 ID |

- 방문목적을 삭제하면 그 아래의 하위 방문목적과 예약도 함께 삭제됩니다.

**Response (204 No Content)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> PURPOSE_NOT_FOUND - 존재하지 않는 방문목적입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 4. 방문목적 단건 조회
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 특정 방문목적 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/purpose/{id}` |
| **토큰 필요** | X |

**Path Parameters**
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| id | long | 방문목적 ID |

**Response (200 OK)**
```json
{
  "id": 1,
  "purpose": "string",
  "reservationStatus": "NONE"
}
```

**Error List**
<details>
<summary><code>404</code> PURPOSE_NOT_FOUND - 존재하지 않는 방문목적입니다</summary>
</details>

---

### 5. 전체 방문목적 조회 (기존 앱 호환)
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 모든 방문목적 목록 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/purpose/all` |
| **토큰 필요** | X |

- 관리자 웹, 체크인 웹, Flutter V1·V2가 쓰는 기존 배열 응답입니다. 모든 클라이언트가 `/purpose/all/with-sub`로 옮기면 제거합니다.
- 하위 방문목적은 포함하지 않습니다.

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "purpose": "string",
    "reservationStatus": "RESERVATION"
  },
  {
    "id": 2,
    "purpose": "string",
    "reservationStatus": "NONE"
  }
]
```

**Error List**
<details>
<summary><code>500</code> INTERNAL_SERVER_ERROR - 서버 내부 오류가 발생했습니다</summary>
</details>

---

### 5-1. 전체 방문목적 조회 (하위 포함)
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 모든 방문목적 목록과 각 방문목적의 하위 방문목적 목록 조회 |
| **HTTP Method** | `GET` |
| **API Path** | `/purpose/all/with-sub` |
| **토큰 필요** | X |

- 등록된 모든 기관의 방문목적을 돌려준다. 기관별로 걸러지지 않는다.
- 방문목적은 `purposeIndex` 오름차순(같으면 `purposeId` 오름차순), 하위 방문목적은 `subPurposeIndex` 오름차순으로 정렬한다.
- 하위 방문목적이 없는 방문목적은 `subPurposes`가 빈 배열(`[]`)이다.
- 하위 방문목적 이름은 중복될 수 있으므로 `subPurposeId`로 구분한다.
- 등록된 방문목적이 없으면 `purposes`가 빈 배열이다.

**Response (200 OK)**
```json
{
  "purposes": [
    {
      "purposeId": 3,
      "purpose": "게임",
      "reservationStatus": "SUB_INCLUDE",
      "subPurposes": [
        {
          "subPurposeId": 1,
          "subPurpose": "PC 1",
          "subPurposeReservation": true
        },
        {
          "subPurposeId": 2,
          "subPurpose": "PC 2",
          "subPurposeReservation": true
        }
      ]
    },
    {
      "purposeId": 5,
      "purpose": "음악실",
      "reservationStatus": "RESERVATION",
      "subPurposes": []
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| purposes | object[] | 방문목적 목록 |
| purposes[].purposeId | long | 방문목적 ID |
| purposes[].purpose | string | 방문목적 이름 |
| purposes[].reservationStatus | string | `RESERVATION`(예약 필요), `NONE`(예약 없이 이용), `SUB_INCLUDE`(하위 방문목적 포함) |
| purposes[].subPurposes | object[] | 하위 방문목적 목록. 없으면 `[]` |
| purposes[].subPurposes[].subPurposeId | long | 하위 방문목적 ID |
| purposes[].subPurposes[].subPurpose | string | 하위 방문목적 이름 |
| purposes[].subPurposes[].subPurposeReservation | boolean | 예약이 필요하면 `true` |

**Error List**
<details>
<summary><code>500</code> INTERNAL_SERVER_ERROR - 서버 내부 오류가 발생했습니다</summary>
</details>

---

### 6. 방문목적 순서 변경
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 방문목적 표시 순서 변경 |
| **HTTP Method** | `PATCH` |
| **API Path** | `/purpose/move` |
| **토큰 필요** | O (Access Token) |

**Request Body**
```json
{
  "purposeId": [1, 3, 2, 4]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| purposeId | long[] | O | 새로운 순서의 ID 배열 |

**Response (200 OK)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>404</code> PURPOSE_NOT_FOUND - 존재하지 않는 방문목적입니다</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>

---

### 7. 하위 방문목적 생성
| 항목 | 내용 |
|------|------|
| **Domain** | Purpose |
| **기능** | 하위 시설이 있는 방문목적에 하위 방문목적 생성 (하위가 없는 방문목적은 방문목적 생성 API 사용) |
| **HTTP Method** | `POST` |
| **API Path** | `/purpose/sub` |
| **토큰 필요** | O (Access Token) |

**Request Body**
```json
{
  "purpose": "게임",
  "reservationStatus": "SUB_INCLUDE",
  "subPurpose": "PC 1",
  "subPurposeReservation": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| purpose | string | O | 상위 방문목적 이름 (1~30자) |
| reservationStatus | string | O | 상위 방문목적 상태. 이 API에서는 항상 `SUB_INCLUDE`로 저장됩니다 |
| subPurpose | string | O | 하위 방문목적 이름 (1~30자) |
| subPurposeReservation | boolean | O | 하위 시설 예약 필요 여부 (`true`: 예약 필요, `false`: 예약 불필요) |

- `purpose`가 기관에 없으면 방문목적을 새로 만들고, 그 아래에 하위 방문목적을 만듭니다. 새 방문목적의 정렬 순서는 방문목적 목록의 맨 뒤로 자동 부여됩니다.
- 이미 있는 방문목적이면 `reservationStatus`를 `SUB_INCLUDE`로 바꾸고 그 아래에 하위 방문목적을 만듭니다.
- `RESERVATION`이던 방문목적이 `SUB_INCLUDE`로 바뀌면 상위 방문목적 단위의 오늘 이후 예약을 삭제합니다. 이후에는 하위 방문목적 단위로 예약합니다.
- 하위 방문목적이 하나라도 있는 방문목적의 `reservationStatus`는 `SUB_INCLUDE`로 고정됩니다.
- 같은 방문목적 안에서 하위 방문목적 이름은 중복될 수 없습니다. 다른 방문목적에는 같은 이름이 있어도 됩니다.
- 하위 방문목적의 정렬 순서는 해당 방문목적의 하위 목록 맨 뒤로 자동 부여됩니다.

**Response (201 Created)**
```
없음 (void)
```

**Error List**
<details>
<summary><code>400</code> BAD_REQUEST - purpose 또는 subPurpose가 비어 있거나 30자를 넘을 때, reservationStatus 또는 subPurposeReservation이 없을 때</summary>
</details>
<details>
<summary><code>401</code> INVALID_TOKEN - 유효하지 않은 토큰입니다</summary>
</details>
<details>
<summary><code>404</code> ORGAN_NOT_FOUND - 해당 기관 계정이 존재하지 않습니다</summary>
</details>
<details>
<summary><code>409</code> SUB_PURPOSE_ALREADY_EXIST - 이미 존재하는 하위 방문목적입니다</summary>
</details>

---

## Error Code 목록

### 인증 관련 에러 (Authentication)
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| INVALID_TOKEN | 401 | 유효하지 않은 토큰입니다 |
| EXPIRED_TOKEN | 401 | 만료된 토큰입니다 |
| REFRESH_TOKEN_NOT_FOUND | 404 | RefreshToken이 존재하지 않습니다 |
| INVALID_ROLE | 401 | 유효하지 않은 역할입니다 |

### 사용자 관련 에러 (User)
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| USER_NOT_FOUND | 404 | 해당 유저가 존재하지 않습니다 |
| USER_MISMATCH | 401 | 유저가 일치하지 않습니다 |
| INVALID_USER | 401 | 유효하지 않은 사용자입니다 |
| USER_EXIST | 401 | 유저가 이미 존재합니다 |
| COMPANION_NOT_FOUND | 404 | 존재하지 않는 동행인 ID입니다 |

### 관리자 관련 에러 (Admin)
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| ADMIN_NOT_FOUND | 404 | 해당 관리자 계정이 존재하지 않습니다 |
| PASSWORD_MISMATCH | 401 | 비밀번호가 일치하지 않습니다 |
| INVALID_PASSWORD_CONFIRM | 401 | 비밀번호 확인이 일치하지 않습니다 |
| SAME_OLD_PASSWORD | 400 | 기존 비밀번호와 동일한 비밀번호로 변경할 수 없습니다 |

### 로그 관련 에러 (Log)
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| LOG_NOT_FOUND | 404 | 존재하지 않는 이용목록입니다 |
| DUPLICATE_LOG | 409 | 이미 해당 시간의 로그가 존재합니다 |

### 방문목적 관련 에러 (Purpose)
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| PURPOSE_NOT_FOUND | 404 | 존재하지 않는 방문목적입니다 |
| PURPOSE_ALREADY_EXIST | 409 | 이미 존재하는 방문 목적입니다 |
| SUB_PURPOSE_ALREADY_EXIST | 409 | 이미 존재하는 하위 방문목적입니다 |
| PURPOSE_STATUS_FIXED | 409 | 하위 방문목적이 있는 방문목적은 예약 상태를 변경할 수 없습니다 |
| SUB_INCLUDE_NOT_ALLOWED | 400 | 하위 방문목적 생성으로만 설정할 수 있는 상태입니다 |

### 기타 에러
| 에러 코드 | HTTP 상태 | 설명 |
|-----------|---------|------|
| INVALID_RESIDENCE | 400     | 존재하지 않는 거주지 이름입니다 |
| EXCEL_GENERATION_FAILED | 507     | 엑셀 파일 생성 중 오류가 발생했습니다 |
| INVALID_DATE | 400     | 유효하지 않은 날짜 형식입니다 |
| BAD_REQUEST | 400     | 잘못된 요청입니다 |
| INTERNAL_SERVER_ERROR | 500     | 서버 내부 오류입니다 |

---

## Enum 정의

### Gender (성별)
| 값 | 설명 |
|----|------|
| MALE | 남성 |
| FEMALE | 여성 |

### Age (나이대)
| 값 | 라벨 |
|----|------|
| TEENS | 10대 |
| TWENTIES | 20대 |
| THIRTIES | 30대 |
| FORTIES | 40대 |
| FIFTIES | 50대 |
| SIXTIES | 60대 |
| SEVENTIES_PLUS | 70대 이상 |

---

## 공통 에러 응답 형식

모든 에러 응답은 다음 형식으로 반환됩니다:

```json
{
  "message": "에러 메시지",
  "status": 400,
  "timestamp": "2026-01-26T15:30:45.123456",
  "description": "상세 설명"
}
```

---

## 시설 예약 (구즉 이용자 화면, 토큰 불필요)

`POST /user/reservation`은 로그인 토큰 없이 호출하는 공개 API입니다. 서비스에서 구즉 기관 ID 1을 사용하여 구즉 회원과 방문 목적만 조회합니다. 컨트롤러에서는 기관 ID를 받지 않습니다.

먼저 `POST /user`에 `{"name":"김정욱"}`을 보내 회원 존재 여부를 확인합니다. 새 회원은 `/user/sign-up`에 선택적으로 `reservationTime`을 보내 회원가입·체크인 후 예약까지 생성할 수 있습니다. 기존 회원은 별도 예약 API를 사용합니다. `duplicateName=true`이면 전화번호를 한 번 더 받아 구분합니다. 예약 과정에서는 회원 조회 응답의 `userId`를 쓰지 않고 이름과 필요 시 전화번호를 `/user/reservation`에 보냅니다.

예약할 단위는 다음 두 가지입니다. 하위 방문목적은 상위가 다르면 이름이 겹칠 수 있으므로 ID로 지정합니다.

| 예약 단위 | 조건 | 지정 방법 |
|------|------|------|
| 상위 방문목적 | `reservationStatus=RESERVATION` | `purposeId` (또는 기존 앱 호환용 `purpose` 이름) |
| 하위 방문목적 | 상위가 `SUB_INCLUDE`이고 `subPurposeReservation=true` | `subPurposeId` (`purposeId`를 함께 보내면 상위가 일치해야 함) |

- 우선순위: `subPurposeId` → `purposeId` → `purpose` 이름. 셋 다 없으면 400 `RESERVATION_TARGET_REQUIRED`입니다.
- `SUB_INCLUDE` 상위 방문목적 자체나 `subPurposeReservation=false`인 하위 방문목적은 400 `RESERVATION_NOT_ALLOWED`입니다.
- 다른 기관의 하위 방문목적이거나 `purposeId`와 상위가 다르면 404 `SUB_PURPOSE_NOT_FOUND`입니다.

### 예약 가능 방문 목적 조회 `GET /user/reservation/purpose`

```json
{
  "purposes": [
    {
      "id": 3,
      "purpose": "게임",
      "reservationStatus": "SUB_INCLUDE",
      "subPurposes": [
        { "subPurposeId": 1, "subPurpose": "PC 1", "subPurposeReservation": true }
      ]
    },
    { "id": 5, "purpose": "음악실", "reservationStatus": "RESERVATION", "subPurposes": [] }
  ]
}
```

- 기존 필드(`id`, `purpose`, `reservationStatus`)는 그대로이고 `subPurposes`가 추가되었습니다.

### 예약 가능 시간 조회 `GET /user/reservation`

| 파라미터 | 타입 | 설명 |
|------|------|------|
| purpose | string | 기존 앱 호환용 방문목적 이름 |
| purposeId | long | 상위 방문목적 ID |
| subPurposeId | long | 하위 방문목적 ID |

- 예: `GET /user/reservation?subPurposeId=1`, `GET /user/reservation?purposeId=5`, `GET /user/reservation?purpose=음악실`
- 응답: `{ "times": ["09:30", "10:30", ...] }` — 오늘 해당 단위에 예약되지 않은 시각

### 예약 생성 `POST /user/reservation`

```json
{
  "purposeId": 3,
  "subPurposeId": 1,
  "name": "김정욱",
  "phone": null,
  "maleCount": 1,
  "femaleCount": 0,
  "startTime": "10:30"
}
```

- 기존 앱은 `"purpose": "음악실"`처럼 이름만 보내도 상위 방문목적 단위로 예약됩니다.
- 예약 날짜는 요청 당일로 저장합니다. 선택 가능한 시작 시각은 09:30~20:30이며, 같은 회원의 여러 예약은 허용하되 같은 예약 단위·날짜·시각에는 한 건만 저장됩니다. 중복 슬롯은 HTTP 409를 반환합니다. 지난 시각인지 서버에서 별도로 검사하지 않습니다.
- 기존 회원은 이 요청으로 예약만 생성합니다. 체크인 기록이나 방문 횟수는 변경하지 않습니다. 일반 목적의 기존 회원은 기존 `POST /user/check-in`을 사용합니다.
- `phone`이 null이거나 빈 값이면 이름만으로, 번호가 있으면 이름을 비교하지 않고 전화번호만으로 구즉 회원을 단건 조회합니다. 전화번호는 입력값과 DB 값 모두 하이픈을 제거하여 대조합니다. 앞 단계에서 회원을 확인한 요청을 전제로 하며, 예약 서비스에서 회원 존재 여부를 재검사하지 않습니다. 동일 조건의 회원이 여러 명이면 JPA 단건 조회 오류가 발생합니다.
- 성공 시 HTTP 201을 반환합니다. 예약에 회원 이름과 대표 전화번호를 저장하며, 번호가 없으면 비워 둡니다. 예약 종료 문자 발송은 아직 연결되지 않았습니다.

### 예약이 정리되는 경우

| 변경 | 정리되는 예약 |
|------|------|
| 방문목적 `RESERVATION` → `NONE` (`PATCH /purpose/{id}`) | 그 방문목적 단위의 오늘 이후 예약 |
| 방문목적 `RESERVATION` → `SUB_INCLUDE` (`POST /purpose/sub`) | 그 방문목적 단위의 오늘 이후 예약 |
| 하위 방문목적 `subPurposeReservation` true → false (`PATCH /purpose/sub/{id}`) | 그 하위 방문목적의 오늘 이후 예약 |
| 하위 방문목적 삭제 (`DELETE /purpose/sub/{id}`) | 그 하위 방문목적의 모든 예약 |
| 방문목적 삭제 (`DELETE /purpose/{id}`) | 그 방문목적과 하위 방문목적의 모든 예약 |

---

## 시설 예약 현황 (관리자, 토큰 필요)

### 오늘 예약 현황 `GET /reservation`

```json
{
  "inUseCount": 2,
  "availableCount": 1,
  "facilities": [
    { "purposeId": 3, "purpose": "게임", "subPurposeId": 1, "subPurpose": "PC 1" },
    { "purposeId": 3, "purpose": "게임", "subPurposeId": 2, "subPurpose": "PC 2" },
    { "purposeId": 5, "purpose": "음악실", "subPurposeId": null, "subPurpose": null }
  ],
  "reservations": [
    {
      "id": 10,
      "purposeId": 3,
      "purpose": "게임",
      "subPurposeId": 1,
      "subPurpose": "PC 1",
      "name": "김정욱",
      "maleCount": 1,
      "femaleCount": 0,
      "phone": "01012345678",
      "startTime": "10:30:00",
      "endTime": "11:30:00"
    }
  ]
}
```

| 필드 | 설명 |
|------|------|
| inUseCount | 현재 시간대의 전체 예약 수 |
| availableCount | 현재 시간대에 비어 있는 예약 단위 수 (`facilities` 수 − 현재 시간대에 예약된 단위 수) |
| facilities | 기관의 예약 가능 단위 평탄 목록. 상위 순서(`purposeIndex`), 하위 순서(`subPurposeIndex`)대로 정렬합니다. 상위 방문목적 단위면 `subPurposeId`·`subPurpose`가 null입니다. |
| reservations | 오늘 예약 목록(시작 시각 오름차순). `purpose`는 상위 방문목적 이름이며, 하위 방문목적 단위 예약이면 `subPurposeId`·`subPurpose`가 채워집니다. |

- 예약과 시설은 이름이 아니라 `(purposeId, subPurposeId)` 쌍으로 연결합니다.

### 예약 생성 `POST /reservation`

`purpose`(이름), `purposeId`, `subPurposeId`로 예약 단위를 지정합니다. 규칙은 이용자 예약 생성과 같고, 나머지 필드(`name`, `phone`, `maleCount`, `femaleCount`, `startTime`)는 기존과 같습니다.

---

## 인증 방식

### JWT Token
- Access Token: API 요청 시 사용
- Refresh Token: Access Token 갱신 시 사용

### 토큰 전달 방식
```
Header:
  Authorization: Bearer {accessToken}

토큰 갱신 시:
  Authentication: {refreshToken}
```

### 토큰 필요 API 표시
- **O**: 토큰 필요 (Authorization 헤더에 Bearer 토큰 포함)
- **X**: 토큰 불필요 (공개 API)
