package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.Application;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

public class ArgumentValidationTest {

    @TempDir
    Path tempDir;

    private Path logPath;
    private Path outputPath;

    @BeforeEach
    public void setupFiles() throws IOException {
        logPath = tempDir.resolve("test.log");
        String logContent =
                """
            127.0.0.1 - - [01/Jan/2025:12:00:00 +0000] "GET /index.html HTTP/1.1" 200 1024 "-" "-"
            """;
        Files.writeString(logPath, logContent);
        outputPath = tempDir.resolve("result.json");
    }

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void shouldFailWhenLocalFileDoesNotExist() throws IOException {
        String[] args = {"--path", "nonexistent.log", "--format", "json", "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void shouldFailWhenRemoteFileDoesNotExist() throws IOException {
        String[] args = {"--path", "https://nonexistent.com", "--format", "json", "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void shouldFailWhenFileHasUnsupportedExtension(String extension) throws IOException {
        Path invalidPath = tempDir.resolve("test" + extension);
        Files.createFile(invalidPath);
        String[] args = {"--path", invalidPath.toString(), "--format", "json", "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void shouldFailWhenFromParameterHasInvalidDateFormat(String from) throws IOException {
        String[] args = {
            "--path", logPath.toString(), "--format", "json", "--output", outputPath.toString(), "--from", from
        };
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = "txt")
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void shouldFailWhenRequestedFormatIsUnsupported(String format) throws IOException {
        String[] args = {"--path", logPath.toString(), "--format", format, "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidOutputFileExtensions")
    @DisplayName("По пути в аргументе --output указан файл с некорректным расширением")
    void shouldFailWhenOutputFileHasInvalidExtension(String format, String output) throws IOException {
        Path invalidOutput = tempDir.resolve(output.substring(2));
        String[] args = {"--path", logPath.toString(), "--format", format, "--output", invalidOutput.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void shouldFailWhenOutputFileAlreadyExists() throws IOException {
        Files.createFile(outputPath);
        String[] args = {"--path", logPath.toString(), "--format", "json", "--output", outputPath.toString()};
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void shouldFailWhenRequiredParameterIsMissing(String argument) throws IOException {
        String[] args;
        if (argument.contains("path") || argument.contains("-p")) {
            args = new String[] {"--format", "json", "--output", outputPath.toString()};
        } else if (argument.contains("output") || argument.contains("-o")) {
            args = new String[] {"--path", logPath.toString(), "--format", "json"};
        } else {
            args = new String[] {"--path", logPath.toString(), "--output", outputPath.toString()};
        }
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void shouldFailWhenUnsupportedParameterIsProvided(String argument) throws IOException {
        String[] args = {
            "--path", logPath.toString(), "--format", "json", "--output", outputPath.toString(), argument, "value"
        };
        CommandLine cmd = new CommandLine(new Application());
        int exitCode = cmd.execute(args);
        assertEquals(2, exitCode);
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void shouldFailWhenFromDateIsGreaterThanToDate() throws IOException {
        String[] args = {
            "--path",
            logPath.toString(),
            "--format",
            "json",
            "--output",
            outputPath.toString(),
            "--from",
            "2025-01-03",
            "--to",
            "2025-01-01"
        };
        int exitCode = new CommandLine(new Application()).execute(args);
        assertEquals(2, exitCode);
    }

    private static Stream<Arguments> provideInvalidOutputFileExtensions() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"));
    }
}
