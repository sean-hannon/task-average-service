package com.example.taskaverages.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskaverages.TaskAverageApplication;
import com.example.taskaverages.support.PostgresIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class TaskMetricsServicePersistenceTest extends PostgresIntegrationTest {

    @Test
    void averagesSurviveApplicationRestart() {
        String taskId = "restart-task-" + UUID.randomUUID();

        try (ConfigurableApplicationContext context = application().run()) {
            TaskMetricsService service = context.getBean(TaskMetricsService.class);
            service.record(taskId, 40);
            service.record(taskId, 80);
        }

        try (ConfigurableApplicationContext context = application().run()) {
            TaskMetricsService service = context.getBean(TaskMetricsService.class);
            TaskAverage average = service.currentAverage(taskId);

            assertThat(average.taskId()).isEqualTo(taskId);
            assertThat(average.averageDurationMillis()).isEqualByComparingTo("60.000");
        }
    }

    private static SpringApplicationBuilder application() {
        return new SpringApplicationBuilder(TaskAverageApplication.class)
                .properties(
                        "spring.main.web-application-type=none",
                        "spring.main.banner-mode=off",
                        "spring.datasource.url=" + postgres().getJdbcUrl(),
                        "spring.datasource.username=" + postgres().getUsername(),
                        "spring.datasource.password=" + postgres().getPassword(),
                        "spring.datasource.driver-class-name=" + postgres().getDriverClassName(),
                        "logging.level.root=warn");
    }
}
