package com.example.taskaverages.repository;

import java.math.BigDecimal;

public record TaskMetricAggregate(String taskId, long sampleCount, BigDecimal totalDurationMillis) {
}
