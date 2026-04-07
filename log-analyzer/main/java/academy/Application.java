package academy;

import academy.parser.LogParser;
import academy.stats.AnalysisResult;
import academy.writers.AdocWriter;
import academy.writers.JsonWriter;
import academy.writers.MarkdownWriter;
import academy.writers.Writer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "log-analyzer", version = "1.0", mixinStandardHelpOptions = true)
public class Application implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(Application.class);

    @Option(
            names = {"-p", "--path"},
            description = "Path to log files (local path with glob pattern or URL)",
            required = true,
            arity = "1..*")
    private List<String> paths;

    @Option(
            names = {"--format", "-f"},
            description = "Output format: json, markdown, adoc",
            required = true)
    private String format;

    @Option(
            names = {"--output", "-o"},
            description = "Path to output file",
            required = true)
    private String output;

    @Option(
            names = {"--from"},
            description = "Start date in ISO8601 format (yyyy-MM-dd)")
    private String from;

    @Option(
            names = {"--to"},
            description = "End date in ISO8601 format (yyyy-MM-dd)")
    private String to;

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Application());

        System.exit(cmd.execute(args));
    }

    @Override
    public Integer call() {
        try {
            validateArguments();

            LogParser parser = new LogParser(paths, from, to);
            AnalysisResult result = parser.processLogs();
            getWriter().write(result);

            logger.info("Analysis completed successfully. Results saved to: {}", output);
            return 0;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid arguments: {}", e.getMessage());
            return 2;
        } catch (IOException e) {
            logger.error("I/O error: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.error("Unexpected error occurred", e);
            return 1;
        }
    }

    private void validateArguments() throws IOException {
        String lowerFormat = format.toLowerCase();
        if (!List.of("json", "markdown", "adoc").contains(lowerFormat)) {
            throw new IllegalArgumentException(
                    "Unsupported output format: " + format + ". Supported formats: json, markdown, adoc");
        }

        if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
            try {
                LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
                LocalDateTime toDate = LocalDateTime.parse(to + "T00:00:00");

                if (!fromDate.isBefore(toDate)) {
                    throw new IllegalArgumentException("Start date (--from) must be before end date (--to)");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Dates must be in ISO8601 format (yyyy-MM-dd)");
            }
        }

        Path outputPath = Path.of(output);

        if (Files.exists(outputPath)) {
            throw new IOException("Output file already exists: " + output);
        }

        Path fileNamePath = outputPath.getFileName();
        if (fileNamePath == null) {
            throw new IllegalArgumentException("Output path has no file name: " + output);
        }

        String fileName = fileNamePath.toString();
        if (!fileName.contains(".")) {
            throw new IllegalArgumentException("Output file has no extension: " + output);
        }

        String expectedExtension =
                switch (format.toLowerCase()) {
                    case "json" -> ".json";
                    case "markdown" -> ".md";
                    case "adoc" -> ".adoc";
                    default -> throw new IllegalArgumentException("Unsupported output format: " + format);
                };

        String actualExtension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        if (!actualExtension.equals(expectedExtension)) {
            throw new IllegalArgumentException("File extension mismatch. Expected " + expectedExtension + " for format "
                    + format + ", but got " + actualExtension);
        }
    }

    private Writer getWriter() throws IOException {
        Path outputPath = Path.of(output);
        BufferedWriter writer = Files.newBufferedWriter(outputPath);

        return switch (format.toLowerCase()) {
            case "json" -> new JsonWriter(writer);
            case "markdown" -> new MarkdownWriter(writer);
            case "adoc" -> new AdocWriter(writer);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}
