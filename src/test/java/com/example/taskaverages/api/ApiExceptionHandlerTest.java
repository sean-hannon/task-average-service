package com.example.taskaverages.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskaverages.service.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleTaskNotFoundReturnsNotFoundProblemDetail() {
        ResponseEntity<ProblemDetail> response = handler.handleTaskNotFound(new TaskNotFoundException("missing-task"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Task average not found");
        assertThat(response.getBody().getDetail())
                .isEqualTo("No durations have been recorded for task 'missing-task'.");
    }

    @Test
    void handleBadRequestReturnsBadRequestProblemDetail() {
        ResponseEntity<ProblemDetail> response = handler.handleBadRequest(new IllegalArgumentException("bad input"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Invalid request");
        assertThat(response.getBody().getDetail()).isEqualTo("bad input");
    }

    @Test
    void handleDataAccessReturnsServiceUnavailableProblemDetail() {
        ResponseEntity<ProblemDetail> response =
                handler.handleDataAccess(new DataAccessResourceFailureException("database unavailable"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Storage unavailable");
        assertThat(response.getBody().getDetail())
                .isEqualTo("The task average store is temporarily unavailable.");
    }
}
