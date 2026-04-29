package backend.academy.linktracker.scrapper.cache;

import backend.academy.linktracker.scrapper.IntegrationTest;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.net.URI;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.ConcurrentHistogram;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CacheLoadTest extends IntegrationTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private LinkRepository linkRepository;

    private final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private final int THREADS = CORE_COUNT * 2;
    private final int USERS_COUNT = 1000;
    private final int LINKS_PER_USER = 100;

    private final long RAMP_UP_DURATION_MS = 60_000;
    private final long STAGE_DURATION_MS = 300_000;

    @Test
    void runFullLoadTest() throws Exception {
        prepareData();

        System.out.println("=== STARTING LOAD TEST ===");
        TestMetrics metrics = executeTest(THREADS, STAGE_DURATION_MS);

        printReport(metrics);
    }

    private TestMetrics executeTest(int threadCount, long durationMs) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        TestMetrics metrics = new TestMetrics();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + durationMs + RAMP_UP_DURATION_MS;

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();

                while (System.currentTimeMillis() < endTime) {
                    long now = System.currentTimeMillis();
                    long userId = random.nextLong(1, USERS_COUNT + 1);

                    boolean isRampUp = (now - startTime) < RAMP_UP_DURATION_MS;

                    int operationType = random.nextInt(101);

                    long opStart = System.nanoTime();
                    try {
                        if (operationType == 0) {
                            linkService.addLink(
                                    userId, URI.create("https://new-link.com/" + random.nextInt()), List.of("tag"));
                            if (!isRampUp) metrics.recordSuccess("POST", System.nanoTime() - opStart);
                        } else {
                            linkService.getByTgChatId(userId);
                            if (!isRampUp) metrics.recordSuccess("GET", System.nanoTime() - opStart);
                        }
                    } catch (Exception e) {
                        if (!isRampUp) metrics.recordError(operationType == 0 ? "POST" : "GET", e);
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(durationMs + RAMP_UP_DURATION_MS + 10, TimeUnit.SECONDS);
        return metrics;
    }

    private void prepareData() throws Exception {
        System.out.println("Preparing 100,000 links...");
        for (long i = 1; i <= USERS_COUNT; i++) {
            for (int j = 0; j < LINKS_PER_USER; j++) {
                linkRepository.add(i, URI.create("https://test.com/" + i + "/" + j), List.of("initial"));
            }
        }
        System.out.println("Data prepared.");
    }

    private void printReport(TestMetrics m) {
        long durationSec = STAGE_DURATION_MS / 1000;
        System.out.println("\n=== ОТЧЕТ ПО НАГРУЗОЧНОМУ ТЕСТИРОВАНИЮ ===");
        printMethodStats("GET /list", m.getStats("GET"), durationSec);
        printMethodStats("POST /list", m.getStats("POST"), durationSec);
    }

    private void printMethodStats(String name, MethodStats s, long duration) {
        System.out.println("\nМетод: " + name);
        System.out.println("RPS: " + (s.totalCalls.sum() / duration));
        System.out.println("Среднее время, мс: " + String.format("%.2f", s.getMeanLatency()));
        System.out.println("50-я перцентиль, мс: " + s.getPercentile(50));
        System.out.println("99-я перцентиль, мс: " + s.getPercentile(99));
        System.out.println("Кол-во 200 OK: " + s.totalCalls.sum());
        System.out.println("Кол-во ошибок (500/502/504): " + s.errorCalls.sum());
    }

    static class TestMetrics {
        private final ConcurrentHashMap<String, MethodStats> stats = new ConcurrentHashMap<>();

        public void recordSuccess(String op, long nanos) {
            stats.computeIfAbsent(op, k -> new MethodStats()).record(nanos);
        }

        public void recordError(String op, Exception e) {
            stats.computeIfAbsent(op, k -> new MethodStats()).errorCalls.increment();
        }

        public MethodStats getStats(String op) {
            return stats.getOrDefault(op, new MethodStats());
        }
    }

    static class MethodStats {
        LongAdder totalCalls = new LongAdder();
        LongAdder errorCalls = new LongAdder();
        ConcurrentHistogram histogram = new ConcurrentHistogram(3);

        void record(long nanos) {
            totalCalls.increment();
            histogram.recordValue(Math.min(nanos / 1_000_000, 60000));
        }

        double getMeanLatency() {
            return histogram.getMean();
        }

        double getPercentile(double p) {
            return histogram.getValueAtPercentile(p);
        }
    }
}
