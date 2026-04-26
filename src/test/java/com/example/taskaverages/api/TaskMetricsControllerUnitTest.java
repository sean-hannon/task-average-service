package com.example.taskaverages.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskaverages.service.TaskAverage;
import com.example.taskaverages.service.TaskMetricsService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TaskMetricsControllerUnitTest {

    @Mock
    private TaskMetricsService taskMetricsService;

    @InjectMocks
    private TaskMetricsController controller;

    @Test
    void taskPerformedDelegatesToServiceAndReturnsOkResponse() {
        ResponseEntity<TaskPerformedResponse> response = controller.taskPerformed(
                "email-digest",
                new TaskPerformedRequest(250L));

        verify(taskMetricsService).record("email-digest", 250L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new TaskPerformedResponse("ok"));
    }

    @Test
    void currentAverageMapsServiceResultToApiResponse() {
        when(taskMetricsService.currentAverage("email-digest"))
                .thenReturn(new TaskAverage("email-digest", new BigDecimal("123.456")));

        TaskAverageResponse response = controller.currentAverage("email-digest");

        verify(taskMetricsService).currentAverage("email-digest");
        assertThat(response).isEqualTo(new TaskAverageResponse("email-digest", new BigDecimal("123.456")));
    }
}
