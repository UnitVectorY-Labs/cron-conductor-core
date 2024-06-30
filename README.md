[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Concept](https://img.shields.io/badge/Status-Concept-white)](https://unitvectory-labs.github.io/uvy-labs-guide/bestpractices/status.html#concept) [![codecov](https://codecov.io/gh/UnitVectorY-Labs/cron-conductor-core/graph/badge.svg?token=FU94BLZKXP)](https://codecov.io/gh/UnitVectorY-Labs/cron-conductor-core)

# cron-conductor-core

Core Java library for cron-conductor providing the interfaces and main logic.

## Getting Started

This library requires Java 17.

It is under development and is not feature complete.

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
