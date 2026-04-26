package com.example.taskaverages.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TaskMetricRepository {

    private static final String UPDATE_SQL = """
            UPDATE task_metrics
               SET sample_count = sample_count + 1,
                   total_duration_millis = total_duration_millis + ?,
                   updated_at = CURRENT_TIMESTAMP
             WHERE task_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public TaskMetricRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void record(String taskId, long durationMillis) {
        BigDecimal duration = BigDecimal.valueOf(durationMillis);
        int updatedRows = updateExistingAggregate(taskId, duration);

        if (updatedRows == 0) {
            insertNewAggregate(taskId, duration);
        }
    }

    public Optional<TaskMetricAggregate> findByTaskId(String taskId) {
        return jdbcTemplate.query(
                        """
                        SELECT task_id, sample_count, total_duration_millis
                          FROM task_metrics
                         WHERE task_id = ?
                        """,
                        (resultSet, rowNumber) -> toAggregate(resultSet),
                        taskId)
                .stream()
                .findFirst();
    }

    private int updateExistingAggregate(String taskId, BigDecimal duration) {
        return jdbcTemplate.update(UPDATE_SQL, duration, taskId);
    }

    private void insertNewAggregate(String taskId, BigDecimal duration) {
        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO task_metrics (task_id, sample_count, total_duration_millis, updated_at)
                    VALUES (?, 1, ?, CURRENT_TIMESTAMP)
                    """,
                    taskId,
                    duration);
        } catch (DuplicateKeyException duplicateKeyException) {
            int updatedRows = updateExistingAggregate(taskId, duration);
            if (updatedRows == 0) {
                throw duplicateKeyException;
            }
        }
    }

    private static TaskMetricAggregate toAggregate(ResultSet resultSet) throws SQLException {
        return new TaskMetricAggregate(
                resultSet.getString("task_id"),
                resultSet.getLong("sample_count"),
                resultSet.getBigDecimal("total_duration_millis"));
    }
}
