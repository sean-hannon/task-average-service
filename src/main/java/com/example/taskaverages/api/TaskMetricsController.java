package com.example.taskaverages.api;

import com.example.taskaverages.service.TaskAverage;
import com.example.taskaverages.service.TaskMetricsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tasks")
class TaskMetricsController {

    static final String TASK_ID_PATTERN = "^[A-Za-z0-9][A-Za-z0-9._:-]{0,127}$";
    private static final String TASK_ID_MESSAGE =
            "must be 1-128 characters, start with a letter or number, and contain only letters, numbers, '.', '_', ':', or '-'";

    private final TaskMetricsService taskMetricsService;

    TaskMetricsController(TaskMetricsService taskMetricsService) {
        this.taskMetricsService = taskMetricsService;
    }

    @PostMapping("/{taskId}/performances")
    ResponseEntity<TaskPerformedResponse> taskPerformed(
            @PathVariable @Pattern(regexp = TASK_ID_PATTERN, message = TASK_ID_MESSAGE) String taskId,
            @Valid @RequestBody TaskPerformedRequest request) {
        taskMetricsService.record(taskId, request.durationMillis());
        return ResponseEntity.ok(new TaskPerformedResponse("ok"));
    }

    @GetMapping("/{taskId}/average")
    TaskAverageResponse currentAverage(
            @PathVariable @Pattern(regexp = TASK_ID_PATTERN, message = TASK_ID_MESSAGE) String taskId) {
        TaskAverage average = taskMetricsService.currentAverage(taskId);
        return new TaskAverageResponse(average.taskId(), average.averageDurationMillis());
    }
}
