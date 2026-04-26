package com.example.taskaverages.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskaverages.repository.TaskMetricAggregate;
import com.example.taskaverages.repository.TaskMetricRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskMetricsServiceTest {

    @Mock
    private TaskMetricRepository taskMetricRepository;

    @InjectMocks
    private TaskMetricsService taskMetricsService;

    @Test
    void recordDelegatesValidDurationToRepository() {
        taskMetricsService.record("email-digest", 250);

        verify(taskMetricRepository).record("email-digest", 250);
    }

    @Test
    void recordRejectsNegativeDuration() {
        assertThatThrownBy(() -> taskMetricsService.record("email-digest", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("durationMillis must be greater than or equal to 0.");

        verify(taskMetricRepository, never()).record("email-digest", -1);
    }

    @Test
    void currentAverageReturnsRoundedAverage() {
        when(taskMetricRepository.findByTaskId("email-digest"))
                .thenReturn(Optional.of(new TaskMetricAggregate(
                        "email-digest",
                        3,
                        BigDecimal.valueOf(100))));

        TaskAverage average = taskMetricsService.currentAverage("email-digest");

        assertThat(average.taskId()).isEqualTo("email-digest");
        assertThat(average.averageDurationMillis()).isEqualByComparingTo("33.333");
    }

    @Test
    void currentAverageThrowsWhenTaskHasNoRecordedDurations() {
        when(taskMetricRepository.findByTaskId("missing-task")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskMetricsService.currentAverage("missing-task"))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("No durations have been recorded for task 'missing-task'.");
    }
}
