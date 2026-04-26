package com.example.taskaverages.service;

import com.example.taskaverages.repository.TaskMetricAggregate;
import com.example.taskaverages.repository.TaskMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class TaskMetricsService {

    private static final int AVERAGE_SCALE = 3;

    private final TaskMetricRepository taskMetricRepository;

    public TaskMetricsService(TaskMetricRepository taskMetricRepository) {
        this.taskMetricRepository = taskMetricRepository;
    }

    public void record(String taskId, long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must be greater than or equal to 0.");
        }

        taskMetricRepository.record(taskId, durationMillis);
    }

    public TaskAverage currentAverage(String taskId) {
        TaskMetricAggregate aggregate = taskMetricRepository.findByTaskId(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        BigDecimal average = aggregate.totalDurationMillis()
                .divide(BigDecimal.valueOf(aggregate.sampleCount()), AVERAGE_SCALE, RoundingMode.HALF_UP);

        return new TaskAverage(aggregate.taskId(), average);
    }
}
