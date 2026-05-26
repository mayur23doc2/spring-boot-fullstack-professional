# HRMS Backend - Worker Attendance and Overtime Engine

This project extends the base Spring Boot HRMS with construction worker attendance, overtime computation, Redis-backed active attendance cache, and transactional monthly settlement.

## Tech Stack
- Java 17
- Spring Boot 3.3.x
- Spring Data JPA / Hibernate
- PostgreSQL (Supabase compatible)
- Redis
- Maven

## Implemented Features

### Part 1 - Attendance + Overtime
- New entities: `Worker`, `Site`, `AttendanceLog`, `OvertimeEntry`
- Enums: `Designation`, `SettlementStatus`
- Full DB constraints with indexes, unique constraints, nullable controls
- Attendance APIs:
  - `POST /api/attendance/clock-in`
  - `POST /api/attendance/clock-out`
  - `GET /api/attendance/active` (Redis only)
  - `GET /api/attendance/log` (paginated)
- Overtime APIs:
  - `GET /api/overtime/summary/{workerId}?month=YYYY-MM`
  - `POST /api/overtime/settle/{workerId}?month=YYYY-MM`
- Business rules implemented:
  - active worker/site validation
  - no double clock-in
  - no clock-out without active clock-in
  - standard shift 8h
  - overtime above 8h
  - first 2h @ 1.5x hourly, remaining @ 2x hourly
  - monthly overtime cap 60h (amount capped)
  - shift > 16h flagged
  - no current month settlement
  - settlement atomic with transaction

### Part 2 - Fix Tickets
- LF-201: Configurable CORS via `application.yml`, security preflight support
- LF-202: Redis timeouts, `CacheErrorHandler`, graceful Redis degradation
- LF-203: Paginated attendance log API + `@EntityGraph` to avoid N+1
- LF-204: `@Transactional` settlement + `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)` for post-commit SMS trigger
- LF-205: Staging profile with Supabase pooler settings, Hikari tuning, HTTP client timeouts, transaction-safe design

## Configuration

### Local (`application.yml`)
- PostgreSQL datasource
- Redis host/port and timeout
- CORS allowlist (`app.cors-allowed-origins`)
- pagination limits (`app.default-page-size`, `app.max-page-size`)

### Staging (`application-staging.yml`)
Use Supabase transaction pooler URL and port `6543`:
- `spring.datasource.url=jdbc:postgresql://<SUPABASE_POOLER_HOST>:6543/postgres?sslmode=require`
- set username/password from Supabase
- tuned Hikari values:
  - `maximum-pool-size`
  - `connection-timeout`
  - `idle-timeout`
  - `max-lifetime` (shorter than Supabase idle close)
  - `keepalive-time`

## Redis Behavior
- On clock-in, key `attendance:active:{workerId}` is written with TTL 16h
- Value includes worker/site/timestamp payload
- On clock-out, key is removed
- Cache failures are logged and never crash DB-backed endpoints
- Active endpoint returns graceful degraded error if Redis is down

## API Examples (curl)

```bash
curl -X POST http://localhost:8080/api/attendance/clock-in \
  -H "Content-Type: application/json" \
  -d '{"workerId":1,"siteId":1}'
```

```bash
curl -X POST http://localhost:8080/api/attendance/clock-out \
  -H "Content-Type: application/json" \
  -d '{"workerId":1}'
```

```bash
curl "http://localhost:8080/api/attendance/active"
```

```bash
curl "http://localhost:8080/api/attendance/log?workerId=1&from=2026-05-01&to=2026-05-25&page=0&size=20"
```

```bash
curl "http://localhost:8080/api/overtime/summary/1?month=2026-04"
```

```bash
curl -X POST "http://localhost:8080/api/overtime/settle/1?month=2026-04"
```

## Error Response Format

```json
{
  "error": "DUPLICATE_CLOCK_IN",
  "message": "Worker is already clocked in at Site: Greenfield Phase 2",
  "timestamp": "2026-05-25T10:30:00Z"
}
```

## Build

```bash
./mvnw -P "!build-frontend" -DskipTests compile
```

## AI Tools Used
- GPT-5 Codex desktop agent for code generation, refactoring, and integration.

## Design Notes
- Controllers return DTOs only.
- Business logic in service layer; repositories DB-only.
- Constructor injection throughout.
- Attendance cache isolated in dedicated Redis service for fault-tolerant behavior.
- Overtime settlement publish/notify decoupled from transaction commit.
