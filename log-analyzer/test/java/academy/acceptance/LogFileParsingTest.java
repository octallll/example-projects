package academy.acceptance;

import static org.junit.jupiter.api.Assertions.*;

import academy.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class LogFileParsingTest {
    @TempDir
    Path tempDir;

    private Path logPath;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        logPath = tempDir.resolve("access.log");
        outputPath = tempDir.resolve("result.json");
    }

    private int run(String... args) {
        return new CommandLine(new Application()).execute(args);
    }

    private Map<String, Object> readJsonResult() throws IOException {
        String content = Files.readString(outputPath);
        return new ObjectMapper().readValue(content, Map.class);
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws IOException {
        String logContent =
                """
                127.0.0.1 - - [01/Jan/2025:10:00:00 +0000] "GET /home HTTP/1.1" 200 1500 "-" "Mozilla"
                127.0.0.1 - - [01/Jan/2025:11:00:00 +0000] "POST /api HTTP/1.1" 201 800 "-" "Bot"
                127.0.0.1 - - [01/Jan/2025:12:00:00 +0000] "GET /home HTTP/1.1" 200 1500 "-" "Mozilla"
                """;

        Files.writeString(logPath, logContent);

        int exitCode = run(
                "--path", logPath.toString(),
                "--output", outputPath.toString(),
                "--format", "json");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputPath));

        Map<String, Object> result = readJsonResult();
        assertEquals(3, result.get("totalRequestsCount"));

        @SuppressWarnings("unchecked")
        Map<String, Object> sizeStat = (Map<String, Object>) result.get("responseSizeInBytes");
        assertEquals(1266.67, sizeStat.get("average"));
        assertEquals(1500.0, sizeStat.get("max"));
        assertEquals(1500.0, sizeStat.get("p95"));
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws IOException {
        String url = "https://gist.githubusercontent.com/nickpeihl/31ebb3f98728715d199df042c975a018/raw/index.js";

        int exitCode = run("--path", url, "--output", outputPath.toString(), "--format", "json");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputPath));

        Map<String, Object> result = readJsonResult();
        Integer total = (Integer) result.get("totalRequestsCount");
        assertEquals(0, total);
    }

    @Test
    @DisplayName(
            "На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws IOException {
        String logContent =
                """
                127.0.0.1 - - [01/Jan/2025:08:00:00 +0000] "GET /old HTTP/1.1" 200 100 "-" "-"
                127.0.0.1 - - [05/Jan/2025:12:00:00 +0000] "GET /api HTTP/1.1" 200 200 "-" "-"
                127.0.0.1 - - [10/Jan/2025:15:00:00 +0000] "POST /data HTTP/1.1" 201 300 "-" "-"
                127.0.0.1 - - [15/Jan/2025:18:00:00 +0000] "GET /new HTTP/1.1" 200 400 "-" "-"
                """;

        Files.writeString(logPath, logContent);

        int exitCode = run(
                "--path", logPath.toString(),
                "--from", "2025-01-05",
                "--to", "2025-01-12",
                "--output", outputPath.toString(),
                "--format", "json");

        assertEquals(0, exitCode);

        Map<String, Object> result = readJsonResult();
        assertEquals(2, result.get("totalRequestsCount"));
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws IOException {
        String logContent =
                """
                127.0.0.1 - - [01/Jan/2025:10:00:00 +0000] "GET /good HTTP/1.1" 200 1000 "-" "-"
                217.0.0.1
                127.0.0.1 - - [01/Jan/2025:11:00:00 +0000] "POST /api HTTP/1.1" 201 500 "-" "-"
                не лог
                тоже не лог
                127.0.0.1 - - [01/Jan/2025:12:00:00 +0000] "GET /end HTTP/1.1" 200 1500 "-" "-"
                """;

        Files.writeString(logPath, logContent);

        int exitCode = run(
                "--path", logPath.toString(),
                "--output", outputPath.toString(),
                "--format", "json");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputPath));

        Map<String, Object> result = readJsonResult();
        assertEquals(3, result.get("totalRequestsCount"));
    }
}
