package com.example.taskaverages.service;

import java.math.BigDecimal;

public record TaskAverage(String taskId, BigDecimal averageDurationMillis) {
}
