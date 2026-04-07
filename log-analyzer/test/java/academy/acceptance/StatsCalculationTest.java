package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

public class StatsCalculationTest {
    @TempDir
    Path tempDir;

    private Path logPath;
    private Path outputPath;

    @BeforeEach
    void setUp() {
        logPath = tempDir.resolve("test.log");
        outputPath = tempDir.resolve("result.json");
    }

    @Test
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() throws IOException {
        String logContent =
                """
                127.0.0.1 - - [01/Jan/2025:12:00:00 +0000] "GET /index.html HTTP/1.1" 200 1000 "-" "-"
                127.0.0.1 - - [01/Jan/2025:13:00:00 +0000] "GET /about.html HTTP/1.1" 200 2000 "-" "-"
                127.0.0.1 - - [01/Jan/2025:14:00:00 +0000] "POST /contact.html HTTP/1.1" 404 0 "-" "-"
                """;
        Files.writeString(logPath, logContent);

        String[] args = {"--path", logPath.toString(), "--format", "json", "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(0, exitCode);

        assertTrue(Files.exists(outputPath));
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> result = mapper.readValue(Files.readString(outputPath), Map.class);
        assertEquals(3, result.get("totalRequestsCount"));

        Map<?, ?> responseSize = (Map<?, ?>) result.get("responseSizeInBytes");
        assertEquals(1000.0, responseSize.get("average"));
        assertEquals(2000.0, responseSize.get("max"));
        assertEquals(2000.0, responseSize.get("p95"));
    }
}
