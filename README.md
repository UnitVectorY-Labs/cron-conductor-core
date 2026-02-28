[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Concept](https://img.shields.io/badge/Status-Concept-white)](https://guide.unitvectorylabs.com/bestpractices/status/#concept) [![codecov](https://codecov.io/gh/UnitVectorY-Labs/cron-conductor-core/graph/badge.svg?token=FU94BLZKXP)](https://codecov.io/gh/UnitVectorY-Labs/cron-conductor-core)

# cron-conductor-core

Modular Java library for high cardinality cron job scheduling with pluggable database and publisher backends.

## Getting Started

This library requires Java 17.

It is under development and is not feature complete.

## Module Structure

The project is organized as a multi-module Maven project:

### common

Core module containing shared models, interfaces, and cron logic.

- **ScheduleEntry** - Data model representing a scheduled job
- **ScheduleType** - Enum for schedule types (CRON, ONCE)
- **CronConductor** - Cron expression validation and next execution time computation
- **ScheduleRepository** - Interface for database persistence operations
- **SchedulePublisher** - Interface for publishing schedule execution events

### database-postgres

PostgreSQL implementation of the `ScheduleRepository` interface using JDBC.

### publisher-gcp-pubsub

GCP Pub/Sub implementation of the `SchedulePublisher` interface for publishing schedule execution events as JSON messages.

### api

API component providing the `ScheduleService` for managing schedule entries. Supports creating, retrieving, listing, and deleting schedules. Validates inputs and computes next execution times for CRON type schedules.

### runner

Runner component providing the `ScheduleRunner` responsible for executing due cron jobs. Queries for due schedules, publishes execution events, updates CRON schedules with the next run time, and removes completed ONCE schedules.

## ScheduleEntry

The `ScheduleEntry` object represents a single schedule within the CronConductor system. Each schedule is uniquely identified and can be either a recurring or a one-time event.

### Fields

- `scheduleId` (String): The UUID for the schedule.
- `namespace` (String): Divides schedules into independent collections, allowing for multi-tenancy.
- `timezone` (String): The timezone in which the schedule is defined.
- `cron` (String): The cron expression for recurring schedules. Not applicable for one-time schedules.
- `runAt` (String): For one-time schedules, this specifies when the schedule will execute. For recurring schedules, it is automatically populated with the next execution time.
- `scheduleType` (ScheduleType): The type of the schedule, which can be either `CRON` for recurring schedules or `ONCE` for one-time schedules.
- `resourceId` (String): The high cardinality identifier for a schedule, such as a specific user or device ID.
- `scheduleName` (String): The low cardinality identifier that belongs to a `resourceId`, providing a unique identifier for the schedule.
- `payload` (JSON Object): The payload for the schedule treated as a JSON object.

### Example JSON Payload

#### Recurring Schedule

```json
{
  "scheduleId": "123e4567-e89b-12d3-a456-426614174000",
  "namespace": "default",
  "timezone": "UTC",
  "cron": "0 * * * *",
  "scheduleType": "CRON",
  "resourceId": "user123",
  "scheduleName": "DailyBackup",
  "payload": { "task": "backup", "scope": "full" }
}
```

#### One-Time Schedule

```json
{
  "scheduleId": "789e0123-e45b-67f8-a456-426614174000",
  "namespace": "default",
  "timezone": "UTC",
  "runAt": "2024-12-31T23:59",
  "scheduleType": "ONCE",
  "resourceId": "server456",
  "scheduleName": "EndOfYearReport",
  "payload": { "report": "yearly", "department": "finance" }
}
```
