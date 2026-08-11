# API 공통 규칙
기능별 API의 세부 요청과 응답은 다음 문서에서 관리한다.

- [인증 API](./auth.md)
- [관리자 수집 관리 API](./admin-collection.md)
- [사용자 데이터 분석 API](./analysis.md)

#### 외부 API 연동
- [초단기예보 연동 명세](./kma/ultra-short.md)
- [단기예보 연동 명세](./kma/short-forecast.md)
- [중기예보 연동 명세](./kma/mid-forecast.md)

## 기본 경로

```text
/api
```

## 요청과 응답 형식

요청 본문과 응답 본문은 JSON을 사용한다.

```http
Content-Type: application/json
```

## 인증

로그인 API를 제외한 요청은 JWT Access Token을 사용한다.

```http
Authorization: Bearer {accessToken}
```

## 권한

| 권한 | 설명 |
|---|---|
| `ADMIN` | 수집 대상 및 수집 이력 관리 |
| `USER` | 수집된 데이터 조회 및 분석 |

## 날짜와 시간

API의 날짜와 시간은 ISO 8601 형식을 사용한다.

```text
2026-08-07T10:30:00+09:00
```

외부 데이터의 관측 시각과 시스템 수집 시각은 구분한다.

## 페이징

목록 데이터는 필요에 따라 다음 파라미터를 사용한다.

| 이름 |              기본값 | 설명               |
|---|-----------------:|------------------|
| `page` |              `0` | 페이지 번호 |
| `size` |             `20` | 페이지당 데이터 수       |
| `sort` | `startedAt,desc` | 정렬할 필드, 정렬 방향    |

예시:

```http
GET /api/admin/collection-executions?page=0&size=20&sort=startedAt,desc
```

### 응답 필드

| 필드 | 예시 | 의미 |
|---|---|---|
| `content` | 배열 | 현재 페이지에 포함된 실제 데이터 목록 |
| `page` | `0` | 현재 페이지 번호 |
| `size` | `20` | 요청한 페이지당 데이터 개수 |
| `totalElements` | `35` | 전체 데이터 개수 |
| `totalPages` | `2` | 전체 페이지 수 |
| `first` | `true` | 현재 페이지가 첫 페이지인지 여부 |
| `last` | `false` | 현재 페이지가 마지막 페이지인지 여부 |

### 응답 형식

```json
{
  "content": [
    {
      "...": "조회 데이터"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 35,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

## 오류 응답

```json
{
  "code": "COLLECTION_TARGET_NOT_FOUND",
  "message": "수집 대상을 찾을 수 없습니다.",
  "timestamp": "2026-08-07T10:30:00+09:00"
}
```

입력값 검증 오류가 필요한 경우 필드 오류 정보를 추가한다.

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다.",
  "fieldErrors": [
    {
      "field": "name",
      "reason": "수집 대상명은 필수입니다."
    }
  ]
}
```

## 주요 HTTP 상태 코드

| 상태 코드 | 사용 기준 |
|---|---|
| `200` | 조회 및 수정 성공 |
| `201` | 리소스 생성 성공 |
| `202` | 수집 작업 요청 접수 |
| `400` | 요청값 오류 |
| `401` | 인증 실패 |
| `403` | 권한 부족 |
| `404` | 대상 없음 |
| `409` | 중복 또는 상태 충돌 |
| `500` | 서버 내부 오류 |