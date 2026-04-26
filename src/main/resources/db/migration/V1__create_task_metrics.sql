CREATE TABLE task_metrics (
    task_id VARCHAR(128) PRIMARY KEY,
    sample_count BIGINT NOT NULL CHECK (sample_count > 0),
    total_duration_millis NUMERIC(38, 0) NOT NULL CHECK (total_duration_millis >= 0),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_metrics_updated_at ON task_metrics (updated_at);
