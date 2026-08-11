# 인증 API

## 1. 로그인

사용자의 로그인 아이디와 비밀번호를 검증하고 Access Token을 발급한다.

```http
POST /api/auth/login
```

### 요청 본문

```json
{
  "loginId": "admin",
  "password": "password"
}
```

### 요청 필드

| 필드 | 타입 | 필수  | 설명 |
|---|---|-----|---|
| `loginId` | String | Y   | 사용자 로그인 아이디 |
| `password` | String | Y   | 사용자 비밀번호 |

### 성공 응답

```http
200 OK
```

```json
{
  "accessToken": "...",
  "user": {
    "loginId": "admin",
    "name": "관리자",
    "role": "ADMIN"
  }
}
```

### 실패 응답

| 상태 코드 | 코드 | 발생 조건 |
|---|---|---|
| `400` | `INVALID_REQUEST` | 필수값 누락 또는 요청 형식 오류 |
| `401` | `INVALID_CREDENTIALS` | 로그인 아이디 또는 비밀번호 불일치 |

---

## 2. 로그인 사용자 정보 조회

Access Token을 기준으로 현재 로그인한 사용자의 정보를 조회한다.

페이지 새로고침 후 사용자 정보와 권한을 복원하거나, 현재 토큰의 유효성을 확인할 때 사용한다.

```http
GET /api/auth/me
```

### 성공 응답

```http
200 OK
```

```json
{
  "loginId": "admin",
  "name": "관리자",
  "role": "ADMIN"
}
```

### 실패 응답

| 상태 코드 | 코드 | 발생 조건 |
|---|---|---|
| `401` | `UNAUTHORIZED` | 토큰이 없거나 유효하지 않음 |
| `404` | `USER_NOT_FOUND` | 토큰의 사용자 정보가 존재하지 않음 |

---