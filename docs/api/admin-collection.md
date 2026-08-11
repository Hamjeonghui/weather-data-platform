# 관리자 수집 관리 API

## 1. 수집 대상 목록

각 수집자료의 설정과 최신 상태를 함께 표시한다.

```http
GET /api/admin/collection-targets
```

### 성공 응답

```http
200 OK
```

```json
{
  "items": [
    {
      "id":1,
      "dataCode":"ULTRA_SHORT_OBSERVATION",
      "dataNameKo":"초단기실황",
      "enabled":true,
      "scheduleType":"MINUTE",
      "intervalValue":10,
      "lastExecutedAt":"2026-08-05T14:50:00",
      "nextExecutedAt":"2026-08-05T15:00:00",
      "latestStatus":"SUCCESS"
    }
  ]
}
```
### 실패 응답

| 상태 코드 | 코드  | 발생 조건 |
|---|-----|---|
| `403` | `FORBIDDEN` |  권한 부족 |
---
## 2. 수집 대상 상세

수집 대상의 상세 설정과 최근 수집 결과를 조회한다.

```http
GET /api/admin/collection-targets/{targetId}
```

### 요청 본문

```json
{
  "targetId": 1
}
```

### 요청 필드

| 필드 | 타입      | 필수  | 설명     |
|---|---------|-----|--------|
| `targetId` | Integer | Y   | 수집구분번호 |

### 성공 응답
```http
200 OK
```

```json
{
  "id": 1,
  "dataCode": "ULTRA_SHORT_OBSERVATION",
  "dataNameKo": "초단기실황",
  "enabled": true,
  "scheduleType": "MINUTE",
  "intervalValue": 10,
  "lastExecutedAt": "2026-08-05T14:50:00+09:00",
  "nextExecutedAt": "2026-08-05T15:00:00+09:00",
  "latestStatus": "SUCCESS"
}
```
### 실패 응답

| 상태 코드 | 코드          | 발생 조건 |
|---|-------------|---|
| `403` | `FORBIDDEN` |  권한 부족 |

---
## 3. 수집 설정 변경

수집 여부와 주기를 관리자가 변경할 수 있다.

```http
PATCH /api/admin/collection-targets/{targetId}
```

### 요청 본문

```json
{
  "enabled":true,
  "scheduleType":"HOUR",
  "intervalValue":1,
  "executionTime":null
}
```

### 요청 필드

| 필드 | 타입      | 필수  | 설명     | 기본값  |
|---|---------|-----|--------|------|
| `enabled` | Boolean | Y   | 활성화 여부 |      |
| `scheduleType` | String  | Y   | 수행 단위  |      |
| `intervalValue` | Integer | N   | 수행 시간  | null |
| `executionTime` | String  | N   | 실행 시간  | null |

### 실패 응답

| 상태 코드 | 코드                | 발생 조건              |
|---|-------------------|--------------------|
| `400` | `INVALID_REQUEST` | 필수값 누락 또는 요청 형식 오류 |
| `403` | `FORBIDDEN`       |  권한 부족             |

---

## 4. 수집 현황 요약

최근 수집 상태와 성공·실패 건수를 한 번에 조회한다.
```http
GET /api/admin/collection-dashboard/summary?from=2026-08-01&to=2026-08-05
```

### 성공 응답

```http
200 OK
```

```json
{
  "targetCount":3,
  "enabledTargetCount":2,
  "runningCount":0,
  "successCount":142,
  "failedCount":3,
  "latestCollectedAt":"2026-08-05T14:50:00"
}
```

### 실패 응답

| 상태 코드 | 코드 | 발생 조건 |
|---|---|---|
| `400` | `INVALID_REQUEST` | 요청 형식 오류 |
| `403` | |  권한 부족             |

---

## 5. 수집 이력 목록

조건에 해당하는 수집 작업의 이력을 조회한다.
```http
GET /api/admin/collection-jobs
    ?targetId=1
    &status=FAILED
    &triggerType=SCHEDULED
    &from=2026-08-01T00:00:00+09:00
    &to=2026-08-05T23:59:59+09:00
    &page=0
    &size=20
    &sort=startedAt,desc
```

### 요청 필드

| 필드 | 타입     | 필수  | 설명            | 기본값              |
|---|--------|-----|---------------|------------------|
| `targetId` | Long | N   | 수집 아이디        | 전체               |
| `status` | String | N   | 수집 현황         | 전체               |
| `triggerType` | String | N   | 수집 조건         | 전체               |
| `from` | LocalDateTime   | N   | 검색 시작일시       | 당일 00:00         |
| `to` | LocalDateTime   | N   | 검색 종료일시       | 현재시각             |
| `page` | Integer | N   | 페이지 번호        | `0`              |
| `size` | Integer | N   | 페이지당 데이터 수    | `20`             |
| `sort` | String | N   | 정렬할 필드, 정렬 방향 | `startedAt,desc` |

### 성공 응답

```http
200 OK
```

```json
{
  "items": [
    {
      "id":101,
      "targetId":1,
      "targetName":"초단기실황",
      "status":"FAILED",
      "triggerType":"SCHEDULED",
      "startedAt":"2026-08-05T14:40:00",
      "finishedAt":"2026-08-05T14:40:05",
      "receivedCount":0,
      "savedCount":0,
      "duplicateCount":0,
      "errorCode":"EXTERNAL_API_TIMEOUT"
    }
  ],
  "page":0,
  "size":20,
  "totalElements":3,
  "totalPages":1,
  "first": true,
  "last": false
}
```

### 실패 응답

| 상태 코드 | 코드                | 발생 조건 |
|---|-------------------|---|
| `400` | `INVALID_REQUEST` | 필수값 누락 또는 요청 형식 오류 |
| `403` | `FORBIDDEN`       |  권한 부족             |

---
## 6. 수집 이력 상세 및 실패 사유

특정 수집 작업에 대한 상세 이력을 조회한다.
```http
GET /api/admin/collection-jobs
```
### 요청 본문

```json
{
  "jobId": 1
}
```

### 요청 필드

| 필드 | 타입      | 필수  | 설명    |
|---|---------|-----|-------|
| `jobId` | Integer | Y   | 작업번호  |

### 성공 응답

```http
200 OK
```

```json
{
  "id":101,
  "targetName":"초단기실황",
  "status":"FAILED",
  "triggerType":"SCHEDULED",
  "startedAt":"2026-08-05T14:40:00",
  "finishedAt":"2026-08-05T14:40:05",
  "receivedCount":0,
  "savedCount":0,
  "duplicateCount":0,
  "errorCode":"EXTERNAL_API_TIMEOUT",
  "errorMessage":"외부 API 응답 제한 시간을 초과했습니다.",
  "retryable":true,
  "retryOfJobId":null
}
```

### 실패 응답

| 상태 코드 | 코드                | 발생 조건             |
|---|-------------------|-------------------|
| `400` | `INVALID_REQUEST` | 필수값 누락 또는 요청 형식 오류 |
| `403` | `FORBIDDEN` | 권한 부족             |
| `404` | `COLLECTION_TARGET_NOT_FOUND` | 수집 대상이 존재하지 않음    |

---

## 7. 수동 수집 실행

수집 대상 자체를 즉시 실행한다.
```http
POST /api/admin/collection-targets/{targetId}/execute
```

### 요청 필드

| 필드 | 타입      | 필수  | 설명     |
|---|---------|-----|--------|
| `targetId` | Integer | Y   | 수집구분번호 |

### 성공 응답

```http
202 Accepted
```

```json
{
  "jobId":102,
  "status":"QUEUED"
}
```

### 실패 응답

| 상태 코드 | 코드              | 발생 조건 |
|---|-----------------|---|
| `400` | `INVALID_REQUEST` | 필수값 누락 또는 요청 형식 오류 |
| `403` |  `FORBIDDEN`               |  권한 부족             |
| `409` | `COLLECTION_ALREADY_RUNNING` | 같은 수집 대상이 이미 실행 중임 |
---