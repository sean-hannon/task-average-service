package com.example.taskaverages.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TaskPerformedRequest(
        @NotNull @PositiveOrZero Long durationMillis
) {
}
