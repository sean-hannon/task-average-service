package com.example.taskaverages.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.taskaverages.support.PostgresIntegrationTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class TaskMetricsServiceConcurrencyTest extends PostgresIntegrationTest {

    @Autowired
    private TaskMetricsService taskMetricsService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM task_metrics");
        executorService = Executors.newFixedThreadPool(8);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void concurrentRecordsAreAccumulatedWithoutLostUpdates() throws Exception {
        int operations = 200;
        CountDownLatch start = new CountDownLatch(1);
        List<Callable<Void>> tasks = IntStream.range(0, operations)
                .mapToObj(ignored -> (Callable<Void>) () -> {
                    start.await();
                    taskMetricsService.record("parallel-task", 10);
                    return null;
                })
                .toList();

        List<Future<Void>> futures = tasks.stream()
                .map(executorService::submit)
                .toList();

        start.countDown();
        for (Future<Void> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT sample_count, total_duration_millis FROM task_metrics WHERE task_id = ?",
                "parallel-task");

        assertThat(((Number) row.get("sample_count")).longValue()).isEqualTo(operations);
        assertThat((BigDecimal) row.get("total_duration_millis"))
                .isEqualByComparingTo(BigDecimal.valueOf(operations * 10L));
    }
}
