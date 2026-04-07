package academy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class ApplicationTest {
    @TempDir
    Path tempDir;

    private Path logPath;

    @BeforeEach
    void setUp() {
        logPath = tempDir.resolve("access.log");
    }

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest() throws IOException {
        String logContent =
                """
            127.0.0.1 - - [01/Jan/2025:10:00:00 +0000] "GET /home HTTP/1.1" 200 1000 "-" "Mozilla"
            127.0.0.1 - - [01/Jan/2025:11:00:00 +0000] "POST /api HTTP/1.1" 201 500 "-" "Bot"
            192.168.1.1 - - [01/Jan/2025:12:00:00 +0000] "GET /static/style.css HTTP/1.1" 304 0 "-" "Chrome"
            """;

        Files.writeString(logPath, logContent);

        Path outputPath = tempDir.resolve("report.json");

        int exitCode = new CommandLine(new Application())
                .execute(
                        "--path", logPath.toString(),
                        "--output", outputPath.toString(),
                        "--format", "json");

        assertEquals(0, exitCode);

        assertTrue(Files.exists(outputPath));

        String jsonContent = Files.readString(outputPath);
        Map<String, Object> result = new ObjectMapper().readValue(jsonContent, Map.class);

        assertEquals(3, result.get("totalRequestsCount"), "Должно быть 3 запроса");

        Map<String, Object> sizeStats = (Map<String, Object>) result.get("responseSizeInBytes");
        assertEquals(500.0, sizeStats.get("average"));
        assertEquals(1000.0, sizeStats.get("max"));
        assertEquals(1000.0, sizeStats.get("p95"));

        assertTrue(result.containsKey("resources") && !((List<?>) result.get("resources")).isEmpty());
        assertTrue(result.containsKey("responseCodes") && !((List<?>) result.get("responseCodes")).isEmpty());

        List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");
        assertTrue(resources.stream()
                .anyMatch(r ->
                        r.get("resource").equals("/home") || r.get("resource").equals("/api")));
    }
}
