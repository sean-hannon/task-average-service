package com.example.taskaverages.api;

import java.math.BigDecimal;

public record TaskAverageResponse(String taskId, BigDecimal averageDurationMillis) {
}
