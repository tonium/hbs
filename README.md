# HBS — Real-time Message Platform

Keycloak + JWT + Kafka + Redis + SSE + PostgreSQL 기반의 실시간 메시지 전달 플랫폼

---

## 프로젝트 개요

본 시스템은 다음 목표를 위해 설계되었습니다.

- 사용자 인증/인가 (Keycloak + JWT)
- 프로그램/채널 단위 구독 관리
- Kafka 기반 대량 메시지 fan-out
- SSE 기반 실시간 사용자 전달
- Redis 기반 고속 캐시 및 메시지 재전송
- PostgreSQL 기반 영속성 보장
- MSA 환경에서 분산 이벤트 트래킹

---

## 아키텍처

```
[Client / App / Browser]
        │ Access Token (JWT)
        ▼
[Keycloak]
  ├─ 인증 (Authentication)
  └─ 기본 역할 (Role) 발급
        │
        ▼
[MSA Services]
  ├─ subscription-service    구독/수신 권한 관리, DB 저장, Redis write-through
  ├─ ingestion-service       발행 권한 검증, 구독자 resolve, Kafka fan-out, 비동기 Job
  ├─ sse-gateway             Kafka consume, Redis Streams, SSE push, 재전송
  └─ common-tracking         event_logs / delivery_events 기록 (공통 모듈)
        │
        ▼
[Kafka]  ──────────────────────────────────────────────────────────
        │
        ▼
[Redis]
  ├─ 구독 캐시          sub:{org}:{program}:{channel}:{shard}
  ├─ 발행 권한 캐시      pubAcl:{org}:{subjectType}:{subjectId}
  ├─ SSE 재전송 스트림   stream:user:{userId}
  └─ 트래킹 캐시        evt:msg:{messageId}
        │
        ▼
[PostgreSQL]
  ├─ subscriptions
  ├─ publisher_acl
  ├─ ingest_jobs
  ├─ outbox_events
  ├─ event_logs
  └─ delivery_events
```

### 메시지 처리 흐름

```
발행 요청 → ingestion-service (권한 검증 → job 생성 → 202 Accepted)
                │
                ▼ Worker
            Redis SSCAN으로 구독자 resolve → dedup → Kafka 발행
                │
                ▼
            sse-gateway (Kafka consume → Redis Streams → SSE push)
                │
                ▼
            Client 수신 / Last-Event-ID 기반 재연결 시 재전송
```

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security, OAuth2 Resource Server |
| Persistence | Spring Data JPA, PostgreSQL 16 |
| Cache | Spring Data Redis, Redis 7 |
| Messaging | Spring for Apache Kafka |
| Auth Server | Keycloak |
| Streaming | SSE (Server-Sent Events) |
| Migration | Flyway |
| Build | Gradle (Kotlin DSL) |
| Container | Docker Compose |

---

## 인프라 구성

### 서비스 포트

| 서비스 | 포트 |
|---|---|
| Keycloak | 8080 |
| subscription-service | 8081 |
| ingestion-service | 8082 |
| sse-gateway | 8083 |
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |

### 로컬 실행

```bash
# 인프라 기동
docker compose -f docker/docker-compose.yml up -d

# 서비스 빌드 및 실행
./gradlew :subscription-service:bootRun
./gradlew :ingestion-service:bootRun
./gradlew :sse-gateway:bootRun
```

### 모듈 구성

```
hbs/
├── common-security/       Keycloak Resource Server 공통 설정
├── common-tracking/       이벤트 로그 공통 모듈
├── subscription-service/  구독 관리 서비스
├── ingestion-service/     메시지 발행 서비스
├── sse-gateway/           SSE 전달 게이트웨이
└── docker/                Docker Compose 인프라 정의
```

---

## 참고 문서

- [DEV_GUIDE.md](DEV_GUIDE.md) — 개발 규칙 및 가이드
- [CLAUDE.md](CLAUDE.md) — AI 페어 프로그래밍 지침
