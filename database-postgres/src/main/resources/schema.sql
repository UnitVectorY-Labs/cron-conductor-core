CREATE TABLE IF NOT EXISTS schedule_entries (
    schedule_id   VARCHAR(36)  PRIMARY KEY,
    namespace     VARCHAR(50)  NOT NULL,
    timezone      VARCHAR(50)  NOT NULL,
    cron          VARCHAR(100),
    run_at        VARCHAR(16),
    schedule_type VARCHAR(10)  NOT NULL,
    resource_id   VARCHAR(100) NOT NULL,
    schedule_name VARCHAR(100) NOT NULL,
    payload       TEXT
);

CREATE INDEX IF NOT EXISTS idx_schedule_entries_namespace_resource
    ON schedule_entries (namespace, resource_id);

CREATE INDEX IF NOT EXISTS idx_schedule_entries_namespace_run_at
    ON schedule_entries (namespace, run_at);
