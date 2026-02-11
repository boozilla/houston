<h1>
  <img src="https://github.com/user-attachments/assets/d4a5720c-cda2-46a4-8927-6a7396686a56" width="82" height="auto" align="absmiddle">
  <font size="6"><b>houston</b></font>
</h1>

##### Your Mission Control Center

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Release](https://img.shields.io/github/v/release/boozilla/houston)](https://github.com/boozilla/houston/releases)

버전 관리 시스템(Git)으로 관리되는 Excel 데이터를 로컬 메모리에 로드하고, RDBMS 스타일의 구문으로 조회할 수 있는 gRPC 기반 자산 데이터 서버입니다. Excel(XLSX) 파일을 동적
Protocol Buffer 메시지로 변환하여 고성능 데이터 서빙을 제공하며, GitHub/GitLab 웹훅을 통한 자동 동기화를 지원합니다.

## 주요 기능

- **Excel to Protobuf 변환** - XLSX 파일의 시트를 분석하여 동적으로 Protocol Buffer 스키마를 생성하고, 바이너리 직렬화된 데이터를 gRPC로 스트리밍합니다.
- **SQL 스타일 쿼리** - CQEngine 기반 인메모리 인덱싱으로, SQL과 유사한 구문(`SELECT`, `WHERE`, `ORDER BY`)을 사용하여 자산 데이터를 조회할 수 있습니다.
- **Git 웹훅 자동 동기화** - GitHub 및 GitLab 웹훅을 통해 Excel 파일 변경 시 서버 데이터를 자동으로 갱신합니다.
- **데이터 무결성 검증** - 고유 기본키(UniquePrimary), 시트 간 참조 무결성(SheetLink) 등의 제약조건을 자동으로 검증합니다.
- **다중 스코프 지원** - SERVER/CLIENT 스코프를 분리하여 용도에 맞는 데이터 접근 제어가 가능합니다.
- **파티션** - 하나의 시트를 환경별로 분리하여 관리할 수 있습니다.
- **JWT 인증** - ECDSA(256/384/512) 기반 JWT 토큰 인증을 지원하며, AWS KMS와 통합할 수 있습니다.
- **다중 스토리지 백엔드** - Amazon S3 또는 GitHub 저장소를 데이터 저장소로 사용할 수 있습니다.
- **Gradle 플러그인** - 클라이언트 프로젝트에서 스키마 동기화 및 제약조건 검증을 수행할 수 있는 Gradle 플러그인을 제공합니다.

## 아키텍처

```
XLSX (Git Repository)
        │
        ▼
GitHub/GitLab Webhook ──▶ houston server (Armeria + gRPC)
                                │
                                ▼
                     Parse XLSX (FastExcel)
                                │
                                ▼
                   Generate Protobuf Descriptor
                                │
                                ▼
                  Build In-Memory Index (CQEngine)
                                │
                                ▼
                    Store in Vault (S3 / GitHub)
                                │
                                ▼
                 Client Query via gRPC / HTTP/2
```

## 모듈 구조

```
houston/
├── api/             공유 API 라이브러리 (AssetData, SQL 빌더, 제약조건 인터페이스)
├── container/       클라이언트용 런타임 라이브러리 (쿼리 실행기, 자산 인덱싱)
├── gradle-plugin/   스키마 동기화 및 제약조건 검증용 Gradle 플러그인
├── verifier/        제약조건 검증 기본 구현체
└── src/main/        메인 애플리케이션 (Spring Boot + Armeria + gRPC)
```

| 모듈              | 배포 대상                | 설명                                       |
|-----------------|----------------------|------------------------------------------|
| `api`           | Maven Central        | 공개 API 인터페이스 및 메시지 타입                    |
| `container`     | Maven Central        | 클라이언트 애플리케이션에서 자산 데이터를 조회하기 위한 런타임 라이브러리 |
| `gradle-plugin` | Gradle Plugin Portal | 빌드 시점에 스키마 동기화 및 검증을 수행하는 플러그인           |
| `verifier`      | 내부 사용                | 제약조건 검증 로직 기본 구현                         |

## 기술 스택

| 영역              | 기술                                                |
|-----------------|---------------------------------------------------|
| Language        | Java 24                                           |
| Build           | Gradle 8.11                                       |
| Framework       | Spring Boot 4.0, Armeria 1.36                     |
| RPC             | gRPC 1.79, Protocol Buffers 4.33                  |
| Reactive        | Project Reactor 3.8                               |
| Database        | R2DBC (jasync-mysql)                              |
| In-Memory Query | CQEngine, JSQLParser                              |
| Excel           | FastExcel                                         |
| Auth            | JWT (ECDSA), AWS KMS                              |
| Cloud           | AWS SDK v2 (S3, KMS, Secrets Manager, CloudWatch) |
| Container       | Jib (multi-arch: ARM64/AMD64)                     |

## 시작하기

### 요구 사항

- Java 24+
- MySQL (R2DBC 호환)
- GitHub 또는 GitLab 저장소 (Excel 파일 관리용)

### 빌드

```bash
./gradlew build
```

### 실행

```bash
./gradlew bootRun
```

### Docker 이미지 빌드

Azul Zulu OpenJDK 25 기반의 멀티 아키텍처(ARM64/AMD64) Docker 이미지를 빌드합니다.

```bash
./gradlew jib
```

### Admin 토큰 생성

```bash
./gradlew generateAdminToken
```

## 환경 변수

주요 설정 항목입니다. 전체 예시는 [`examples/environment-variables.env`](examples/environment-variables.env)를 참조하세요.

| 환경 변수                   | 설명                       |
|-------------------------|--------------------------|
| `PROJECT_NAME`          | 프로젝트 이름                  |
| `PACKAGE_NAME`          | 패키지 이름                   |
| `ARMERIA_PORTS_0_PORT`  | 서버 포트 (기본값: 8080)        |
| `BRANCH`                | 웹훅 트리거 대상 Git 브랜치        |
| `GITHUB_ACCESS_TOKEN`   | GitHub 액세스 토큰            |
| `GITHUB_REPO`           | GitHub 저장소 경로            |
| `GITHUB_WEBHOOK_SECRET` | GitHub 웹훅 시크릿            |
| `KEY_ALGORITHM`         | JWT 키 알고리즘 (예: ECDSA256) |
| `KEY_KMS_ID`            | AWS KMS 키 ID (KMS 사용 시)  |
| `SPRING_R2DBC_URL`      | MySQL R2DBC 연결 URL       |
| `SPRING_R2DBC_USERNAME` | DB 사용자명                  |
| `SPRING_R2DBC_PASSWORD` | DB 비밀번호                  |

## gRPC 서비스

### AssetService

자산 데이터 조회 및 스트리밍을 담당합니다.

| RPC          | 설명                      |
|--------------|-------------------------|
| `list`       | 시트 목록 및 메타데이터 스트리밍      |
| `fetchList`  | 전체 시트 목록 일괄 조회          |
| `query`      | SQL 스타일 쿼리를 통한 데이터 스트리밍 |
| `fetchQuery` | 쿼리 결과 일괄 조회             |

### ManifestService

앱 메타데이터 및 유지보수 정보를 제공합니다.

### PluginService (관리자 전용)

스키마 조회 및 제약조건 검증 플러그인을 관리합니다.

## Gradle 플러그인

클라이언트 프로젝트에서 houston 서버와 연동하기 위한 태스크를 제공합니다.

```bash
# 서버로부터 Proto 스키마 동기화
./gradlew houstonSyncSchema

# 자산 데이터 제약조건 검증 실행
./gradlew houstonRunVerifier
```

## whitney

[![whitney release](https://img.shields.io/github/v/release/boozilla/whitney)](https://github.com/boozilla/whitney/releases)

[**whitney**](https://github.com/boozilla/whitney)는 houston과 함께 사용하도록 설계된 고성능 데스크톱 Excel 에디터입니다. 기존 스프레드시트 도구 대신
whitney를 사용하면 houston 워크플로우에 최적화된 데이터 편집 환경을 제공합니다.
