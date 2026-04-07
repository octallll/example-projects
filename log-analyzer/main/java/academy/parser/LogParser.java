package academy.parser;

import static academy.parser.ParserUtils.createReader;
import static academy.parser.ParserUtils.getSources;
import static academy.parser.ParserUtils.isWithinDateRange;
import static academy.parser.ParserUtils.parseIsoDate;

import academy.stats.AnalysisResult;
import academy.stats.StatisticsAccumulator;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LogParser {
    private static final Logger logger = LogManager.getLogger(LogParser.class);
    private static final Pattern LOG_PATTERN =
            Pattern.compile("^([\\d.:a-fA-F]+) - (.+?) \\[(.+?)] \"(.+?)\" (\\d+) (\\d+) \"(.+?)\" \"(.+?)\"$");
    private static final Pattern REQUEST_PATTERN = Pattern.compile("^(\\w+) (.*?) (\\S+)$");
    private static final DateTimeFormatter LOG_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;
    private final List<String> sources;

    public LogParser(List<String> paths, String from, String to) {
        this.fromDate = parseIsoDate(from);
        this.toDate = parseIsoDate(to);
        this.sources = new ArrayList<>();

        for (String path : paths) {
            try {
                List<String> foundSources = getSources(path, List.of("txt", "log"));
                this.sources.addAll(foundSources);
            } catch (Exception e) {
                logger.warn("Error processing path '{}': {}", path, e.getMessage());
            }
        }
    }

    public AnalysisResult processLogs() throws IOException {
        if (this.sources.isEmpty()) {
            throw new IllegalArgumentException("No valid log files found in the provided paths.");
        }

        StatisticsAccumulator stats = new StatisticsAccumulator();
        int skippedLines = 0;

        for (String source : sources) {
            logger.info("Processing file: {}", source);

            try (BufferedReader reader = createReader(source)) {
                String line;

                while ((line = reader.readLine()) != null) {
                    try {
                        LogEntry entry = parseLogLine(line);

                        if (isWithinDateRange(entry.localDateTime(), fromDate, toDate)) {
                            stats.update(entry);
                        }
                    } catch (Exception e) {
                        skippedLines++;
                        logger.debug("Failed to parse log line: {}", line, e);
                    }
                }
            }
        }

        logger.info(
                "Processed {} valid log entries. Skipped {} invalid lines.",
                stats.getTotalRequestsCount(),
                skippedLines);

        return stats.buildResult(sources);
    }

    private LogEntry parseLogLine(String line) throws ParseException {
        Matcher matcher = LOG_PATTERN.matcher(line);

        if (!matcher.matches()) {
            throw new ParseException("Line does not match expected log format", 0);
        }

        return buildLogEntry(matcher);
    }

    private LogEntry buildLogEntry(Matcher matcher) {
        String ip = matcher.group(1);
        String user = matcher.group(2);
        String dateTimeStr = matcher.group(3);
        String request = matcher.group(4);
        int status = Integer.parseInt(matcher.group(5));
        int responseSize = Integer.parseInt(matcher.group(6));
        String referer = matcher.group(7);
        String userAgent = matcher.group(8);

        Matcher reqMatcher = REQUEST_PATTERN.matcher(request);
        String method = "unknown";
        String resource = "unknown";
        String protocol = "unknown";

        if (reqMatcher.matches()) {
            method = reqMatcher.group(1);
            resource = reqMatcher.group(2);
            protocol = reqMatcher.group(3);
        } else {
            logger.warn("Failed to parse request: {}", request);
        }

        ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, LOG_DATE_FORMATTER);
        LocalDateTime dateTime = zdt.toLocalDateTime();

        return new LogEntry(ip, user, dateTime, method, resource, protocol, status, responseSize, referer, userAgent);
    }
}
