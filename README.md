# Weather Data Platform

외부 기상 API 데이터를 주기적으로 수집하고,  
수집 상태를 모니터링하며 수집된 데이터를 조회·분석할 수 있는 웹 플랫폼.

## 주요 기능

### 관리자

- 외부 API 수집 대상 관리
- 수집 상태 모니터링
- 성공/실패 이력 조회
- 실패 사유 확인
- 수동 수집 및 재실행

### 사용자

- 지점별 최신 예보 조회(초단기/단기+중기)
- 특정 기간의 과거 예보 조회(초단기/단기+중기)
- 특정 지점/예보항목의 동일 예측시각에 대한 변화 조회

## 기술 스택

### Backend

- Java 17
- Spring Boot 3.5.4
- Spring Data JPA
- PostgreSQL 17

### Frontend

- React
- TypeScript
- Vite

### Infrastructure

- Docker
- Docker Compose

### 예정

- Spring Security
- JWT
- QueryDSL
- TanStack Query
- Zustand
- GitHub Actions
- AWS EC2
- AWS RDS

> 예정 기술은 프로젝트 진행 과정에서 변경될 수 있음.

## 프로젝트 구조

```text
weather-data-platform/
├── backend/        # Spring Boot API 서버
├── frontend/       # React 웹 애플리케이션
├── docker/         # Docker 관련 설정
├── docs/           # 요구사항 및 개발 설계 문서
├── docker-compose.yml
└── README.md
```

## 로컬 개발 환경

로컬 개발 환경에서는 PostgreSQL만 Docker Compose로 실행하고, Spring Boot와 React는 각각 로컬에서 실행한다.

```text
React : 5173
   ↓ HTTP 요청
Spring Boot : 8080
   ↓ JDBC
PostgreSQL : 5432
```

### 1. PostgreSQL 실행

프로젝트 루트에서:

```bash
docker compose up -d
```

실행 상태 확인:

```bash
docker compose ps
```

종료:

```bash
docker compose stop
```

재시작:

```bash
docker compose start
```

### 2. Spring Boot 실행

새 터미널을 열고 백엔드 디렉터리로 이동한다.

```bash
cd backend
./gradlew bootRun
```

또는 IntelliJ에서 `BackendApplication`을 실행한다.

정상적으로 실행되면 다음 주소에서 서버가 동작한다.

```text
http://localhost:8080
```

> Spring Boot를 실행하기 전에 PostgreSQL 컨테이너가 실행 중이어야 한다.

### 3. React 실행

새 터미널을 열고 프론트엔드 디렉터리로 이동한다.

```bash
cd frontend
npm install
npm run dev
```

`npm install`은 프로젝트를 처음 실행하거나 의존성이 변경된 경우에 실행한다. 이미 설치가 완료된 경우에는 다음 명령만 실행하면 된다.

```bash
npm run dev
```

정상적으로 실행되면 다음 주소에서 화면을 확인할 수 있다.

```text
http://localhost:5173
```

### 실행 순서

```text
1. Docker Desktop 실행
2. PostgreSQL 실행
3. Spring Boot 실행
4. React 실행
```

### 종료

React와 Spring Boot는 각 실행 터미널에서 `Ctrl + C`로 종료한다.

PostgreSQL 컨테이너는 프로젝트 루트에서 다음 명령으로 중지한다.

```bash
docker compose stop
```

## 환경 설정

로컬 Docker 환경변수 파일을 생성한다.

```text
.env.example
    ↓ 복사
.env
```

실제 비밀번호 등의 환경정보가 포함된 `.env` 파일은 Git에 포함하지 않는다.

Spring Boot의 로컬 데이터베이스 연결 설정 역시 별도의 로컬 환경 설정을 사용한다.

## 개발 문서

상세한 요구사항과 설계 내용은 [`docs`](./docs)에서 관리한다.

### 설계

- [요구사항](./docs/01_requirements.md)
- [아키텍처](./docs/02_architecture.md)
- [데이터베이스 설계](./docs/03_database.md)
- [개발 규칙](./docs/04_development-rules.md)

### API

- [API 공통 규칙](./docs/api/conventions.md)
- [인증 API](./docs/api/auth.md)
- [관리자 수집 관리 API](./docs/api/admin-collection.md)
- [사용자 데이터 분석 API](./docs/api/analysis.md)

## 프로젝트 상태

- [x] React 프로젝트 구성
- [x] Spring Boot 프로젝트 구성
- [x] Docker Compose 구성
- [x] PostgreSQL 개발 환경 구성
- [x] Spring Boot - PostgreSQL 연결
- [x] 데이터베이스 설계
- [ ] 백엔드 기본 구조 구성
- [ ] 인증/인가
- [ ] 외부 API 수집
- [ ] 수집 모니터링
- [ ] 데이터 조회 및 분석
- [ ] 테스트
- [ ] CI/CD
- [ ] AWS 배포