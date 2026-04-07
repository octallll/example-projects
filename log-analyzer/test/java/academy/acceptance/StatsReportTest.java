package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

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

public class StatsReportTest {
    @TempDir
    Path tempDir;

    private Path logPath;

    @BeforeEach
    void setUp() throws IOException {
        logPath = tempDir.resolve("access.log");
        Files.writeString(
                logPath,
                """
                127.0.0.1 - - [01/Jan/2025:10:00:00 +0000] "GET /home HTTP/1.1" 200 1000 "-" "Mozilla"
                127.0.0.1 - - [01/Jan/2025:11:00:00 +0000] "GET /api HTTP/1.1" 200 2000 "-" "Bot"
                """);
    }

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() throws IOException {
        Path jsonOutput = tempDir.resolve("result.json");

        int exitCode = new CommandLine(new Application())
                .execute(
                        "--path", logPath.toString(),
                        "--output", jsonOutput.toString(),
                        "--format", "json");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(jsonOutput));

        Map<String, Object> result = new ObjectMapper().readValue(Files.readString(jsonOutput), Map.class);
        assertEquals(2, result.get("totalRequestsCount"));

        Map<String, Object> sizeStat = (Map<String, Object>) result.get("responseSizeInBytes");
        assertEquals(1500.0, sizeStat.get("average"));
        assertEquals(2000.0, sizeStat.get("max"));
        assertEquals(2000.0, sizeStat.get("p95"));

        assertNotNull(result.get("files"));
        assertNotNull(result.get("resources"));
        assertNotNull(result.get("responseCodes"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() throws IOException {
        Path mdOutput = tempDir.resolve("result.md");

        int exitCode = new CommandLine(new Application())
                .execute(
                        "--path", logPath.toString(),
                        "--output", mdOutput.toString(),
                        "--format", "markdown");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(mdOutput));

        String mdContent = Files.readString(mdOutput);

        assertTrue(mdContent.contains("#### Общая информация"));
        assertTrue(mdContent.contains("Метрика"));
        assertTrue(mdContent.contains("Количество запросов"));
        assertTrue(mdContent.contains("2"));

        assertTrue(mdContent.contains("#### Запрашиваемые ресурсы"));
        assertTrue(mdContent.contains("/home") || mdContent.contains("/api"));

        assertTrue(mdContent.contains("#### Коды ответа"));
        assertTrue(mdContent.contains("200"));
        assertTrue(mdContent.contains("OK"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() throws IOException {
        Path adocOutput = tempDir.resolve("result.adoc");

        int exitCode = new CommandLine(new Application())
                .execute(
                        "--path", logPath.toString(),
                        "--output", adocOutput.toString(),
                        "--format", "adoc");

        assertEquals(0, exitCode);
        assertTrue(Files.exists(adocOutput));

        String adocContent = Files.readString(adocOutput);

        System.out.println(adocContent);

        assertTrue(adocContent.contains("=== Общая информация"));
        assertTrue(adocContent.contains("=== Запрашиваемые ресурсы"));
        assertTrue(adocContent.contains("=== Коды ответа"));

        assertTrue(adocContent.contains("[options=\"header\"]"));
        assertTrue(adocContent.contains("|==="));
        assertTrue(adocContent.contains("|==="));

        assertTrue(adocContent.contains("Количество запросов"));
        assertTrue(adocContent.contains("2"));

        assertTrue(adocContent.contains("/home") || adocContent.contains("/api"));

        assertTrue(adocContent.contains("200"));
        assertTrue(adocContent.contains("OK"));

        if (adocContent.contains("=== Распределение запросов по датам")) {
            assertTrue(adocContent.contains("2025-01-01"));
            assertTrue(adocContent.contains("Wednesday"));
        }

        if (adocContent.contains("=== Уникальные протоколы")) {
            assertTrue(adocContent.contains("HTTP/1.1"));
        }
    }
}
