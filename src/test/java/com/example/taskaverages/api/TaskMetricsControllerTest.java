package com.example.taskaverages.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskaverages.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TaskMetricsControllerTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearMetrics() {
        jdbcTemplate.update("DELETE FROM task_metrics");
    }

    @Test
    void recordsDurationsAndReturnsAverage() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/email-digest/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMillis\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        mockMvc.perform(post("/api/v1/tasks/email-digest/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMillis\":300}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tasks/email-digest/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("email-digest"))
                .andExpect(jsonPath("$.averageDurationMillis").value(200.0));
    }

    @Test
    void returnsNotFoundWhenNoDurationsHaveBeenRecorded() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/missing-task/average"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task average not found"));
    }

    @Test
    void rejectsNegativeDurations() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/email-digest/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMillis\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void rejectsInvalidTaskIdentifiers() throws Exception {
        mockMvc.perform(post("/api/v1/tasks/bad task/performances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"durationMillis\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }
}
