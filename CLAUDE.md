# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

houston은 gRPC 기반의 자산(Asset) 데이터 관리 서버입니다. Excel(XLSX) 파일을 동적 Protocol Buffer 메시지로 변환하여 제공하며, GitHub/GitLab 웹훅을 통한 자동
동기화를 지원합니다.

## 빌드 및 테스트 명령어

```bash
# 전체 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :api:test
./gradlew :container:test

# 단일 테스트 실행
./gradlew test --tests "패키지명.테스트클래스명.테스트메서드명"

# Spring Boot 실행
./gradlew bootRun

# Docker 이미지 빌드 (Jib)
./gradlew jib

# Admin 토큰 생성
./gradlew generateAdminToken
```

## 모듈 구조

```
houston/
├── api/           # 공유 API 라이브러리 (AssetData, SQL 빌더, 제약조건 인터페이스)
├── container/     # 클라이언트용 런타임 라이브러리 (houston 컨테이너, 쿼리 실행기)
├── gradle-plugin/ # 스키마 동기화 및 제약조건 검증용 Gradle 플러그인
├── verifier/      # 제약조건 검증 기본 구현체
└── src/main/      # 메인 애플리케이션 (Spring Boot + Armeria + gRPC)
```

## 핵심 아키텍처

**Transport Layer**: Armeria 프레임워크 사용, HTTP/2 + gRPC 지원 (framed/unframed 모두 지원)

**gRPC Services** (`src/main/java/boozilla/houston/grpc/`):

- `AssetGrpc`: 자산 데이터 조회 및 스트리밍
- `ManifestGrpc`: 앱 메타데이터/유지보수 정보
- `PluginGrpc`: 플러그인 관리

**자산 처리** (`src/main/java/boozilla/houston/asset/`):

- `AssetContainer`: XLSX 파일 로드 및 관리
- `AssetSheet`: 시트 메타데이터 및 스키마 정의
- `codec/`: Protobuf, JSON 직렬화
- `constraints/`: 제약조건 (UniquePrimary, SheetLink 등)

**인증** (`src/main/java/boozilla/houston/security/`):

- JWT 기반 인증 (ECDSA256/384/512)
- AWS KMS 통합 지원

**저장소** (`src/main/java/boozilla/houston/repository/vaults/`):

- S3Vaults: Amazon S3 백엔드
- GitHubVaults: GitHub 저장소 백엔드

## 주요 기술 스택

- Java 21, Spring Boot 3.5, Armeria 1.33
- gRPC 1.71, Protobuf 4.30
- Project Reactor (비동기 리액티브)
- R2DBC + jasync-mysql (리액티브 DB)
- CQEngine + JSQLParser (SQL 쿼리 파싱)

## 설정

주요 설정 파일: `src/main/resources/application.yml`
환경변수 예시: `examples/environment-variables.env`

Proto 정의:

- `src/main/proto-public/`: 공개 서비스 정의 (AssetService, ManifestService, PluginService)
- `src/main/proto-internal/`: 내부 메시지 타입

## Gradle 플러그인 태스크

클라이언트 프로젝트에서 사용:

```bash
./gradlew houstonSyncSchema    # houston 서버에 스키마 동기화
./gradlew houstonRunVerifier   # 자산 제약조건 검증 실행
```

## 배포

- Docker: Jib 사용, Azul Zulu OpenJDK 24 기반
- Maven Central: `api`, `container` 모듈 배포
- Gradle Plugin Portal: `gradle-plugin` 배포
