# Task Average Service

RESTful Spring Boot service that continuously tracks the average duration of named tasks.

## Design

- Stores only aggregate state per task: `sample_count` and `total_duration_millis`.
- Persists the aggregate rows in a relational database, so averages are available after restart.
- Uses Flyway migrations for schema management.
- Uses constant-time writes and reads; no historical event replay is needed.
- Uses PostgreSQL for durable storage in local Docker Compose.
- Exposes Spring Boot Actuator health and metrics endpoints.

## API

Record that a task was performed:

```bash
curl -X POST http://localhost:8080/api/v1/tasks/email-digest/performances \
  -H 'Content-Type: application/json' \
  -d '{"durationMillis": 250}'
```

Response:

```json
{"status":"ok"}
```

Get the current average:

```bash
curl http://localhost:8080/api/v1/tasks/email-digest/average
```

Response:

```json
{"taskId":"email-digest","averageDurationMillis":250.000}
```

Unknown tasks return `404`. Invalid task identifiers or negative durations return `400`.

Task identifiers must be 1-128 characters, start with a letter or number, and contain only letters, numbers, `.`, `_`, `:`, or `-`.

## Run

```bash
mvn spring-boot:run
```

The default database URL is:

```text
jdbc:postgresql://localhost:5432/task_metrics
```

Start PostgreSQL first, or provide another PostgreSQL datasource:

```bash
TASK_METRICS_DB_URL=jdbc:postgresql://localhost:5432/task_metrics \
TASK_METRICS_DB_USERNAME=task_metrics \
TASK_METRICS_DB_PASSWORD=task_metrics \
mvn spring-boot:run
```

## Docker Compose

```bash
docker compose up --build
```

The Compose setup starts PostgreSQL and the service, maps the service to `http://localhost:8080`, maps PostgreSQL to `localhost:5432`, and stores database files in the `task-average-postgres-data` Docker volume so averages survive container recreation.

## Test

```bash
mvn test
```

The test suite covers REST behavior, validation, concurrent aggregate updates, and restart persistence.
