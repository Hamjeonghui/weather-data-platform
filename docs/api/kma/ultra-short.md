# 기상청 초단기예보 API

## 기본 정보

- 공식 문서: https://www.data.go.kr/cmm/cmm/fileDownload.do?atchFileId=FILE_000000003671875&fileDetailSn=1
- 확인일: 2026-08-11
- Base URL: `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst`
- 인증 환경변수: `kma.service-key`

## 사용 파라미터

| 외부 필드 | 타입/형식 | 필수 | 설명 | 값 결정 방식 | 내부 출처 | 예시 |
|---|---|---:|---|---|---|---|
| `serviceKey` | String | Y | 공공데이터포털 인증키 | 환경변수에서 읽음 | `kma.service-key` |  |
| `pageNo` | Integer | Y | 페이지 번호 | 첫 요청은 1, 추가 페이지는 1씩 증가 | 수집 로직 | `1` |
| `numOfRows` | Integer | Y | 한 페이지 결과 수 | 프로젝트 고정값 사용 | 애플리케이션 설정 | `1000` |
| `dataType` | String | Y | 응답 데이터 형식 | `JSON` 고정 | 애플리케이션 설정 | `JSON` |
| `base_date` | String, `yyyyMMdd` | Y | 발표일자 | 실행 시점에서 유효한 최근 발표시각 계산 | 수집 스케줄러 | `20260811` |
| `base_time` | String, `HHmm` | Y | 발표시각 | API 발표주기에 따라 계산 | 수집 스케줄러 | `0500` |
| `nx` | Integer | Y | 예보지점 격자 X | 선택된 지점 메타정보 사용 | `location.nx` | `60` |
| `ny` | Integer | Y | 예보지점 격자 Y | 선택된 지점 메타정보 사용 | `location.ny` | `127` |

## 응답 필드 매핑

| 외부 필드 | 내부 필드 | 변환 규칙 | 비고 |
|---|---|---|---|
| `baseDate`, `baseTime` | `base_at` | 두 필드를 결합해 `Asia/Seoul`의 일시로 변환 | 발표시각 |
| `fcstDate`, `fcstTime` | `fcst_at` | 두 필드를 결합해 `Asia/Seoul`의 일시로 변환 | 예측시각 |
| `category`, `fcstValue` | 항목별 내부 컬럼 | `category`에 대응하는 컬럼을 선택한 후 `fcstValue`를 항목별 타입으로 변환 | 아래 조건부 매핑표 참조 |

## 예보항목별 조건부 매핑
| `category` | 예보항목    | 대상 내부 컬럼 | 내부 타입 |
|------------|---------|----------|---|
| `POP`      | 강수확률    | `pop`    | Decimal |
| `RN1`      | 1시간 강수량 | `rn`     | Decimal |
| `REH`      | 습도      | `reh`    | Decimal |
| `T1H`      | 기온  | `tmp`    | Decimal |
| `VEC`      | 풍향      | `wd`     | Decimal |
| `WSD`      | 풍속      | `ws`     | Decimal |
| `LGT`      | 낙뢰      | `lgt`    | Decimal |

## 발표 시각
- 매시간 30분 기준으로 생성(45분 이후 생산완료)
- 10분마다 최신 정보로 업데이트(기온, 습도, 바람)

## 수집 규칙
- 발표자료가 아직 생성되지 않은 경우 이전 발표시각으로 재시도
- 동일한 발표시각·예측시각·지점·항목 데이터는 중복 저장하지 않음