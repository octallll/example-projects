package academy.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserUtils {
    private static final Logger logger = LogManager.getLogger(ParserUtils.class);

    public static BufferedReader createReader(String source) throws IOException {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request =
                        HttpRequest.newBuilder().uri(URI.create(source)).GET().build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    if (response.statusCode() == 404) {
                        logger.error("Remote file not found: {}", source);
                        throw new FileNotFoundException("Remote file not found: " + source);
                    }

                    logger.error("Failed to download remote file: {} (status code: {})", source, response.statusCode());
                    throw new IOException("Failed to download remote file: " + source + " (status code: "
                            + response.statusCode() + ")");
                }

                return new BufferedReader(new InputStreamReader(response.body()));
            } catch (Exception e) {
                logger.error("Error reading remote file: {}", source, e);
                throw new IOException("Error reading remote file: " + source, e);
            }
        } else {
            return Files.newBufferedReader(Path.of(source));
        }
    }

    public static List<String> getSources(String path, List<String> availableExtensions) throws IOException {
        ArrayList<String> sources = new ArrayList<>();

        logger.info("Path: {}", path);

        if (path.startsWith("http://") || path.startsWith("https://")) {
            sources.add(path);
        } else {
            Path p = Path.of(path);

            if (Files.isRegularFile(p)) {
                sources.add(p.toAbsolutePath().toString());
            } else {
                final Path base;
                final String pattern;

                if (p.isAbsolute()) {
                    Path parent = p.getParent();
                    if (parent != null && !Files.exists(parent)) {
                        throw new FileNotFoundException("Directory not found: " + parent);
                    }

                    base = (parent != null) ? parent : p.getRoot();

                    Path fileName = p.getFileName();

                    if (fileName != null) {
                        pattern = fileName.toString();
                    } else {
                        pattern = "*";
                    }
                } else {
                    base = Path.of(".");
                    pattern = path;
                }

                if (base == null) {
                    throw new IllegalStateException("Base path cannot be null for path: " + path);
                }

                PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

                Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Path relative = base.relativize(file);
                        if (matcher.matches(relative) && attrs.isRegularFile()) {
                            sources.add(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        logger.warn("Failed to access file: {}", file, exc);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            for (String src : sources) {
                int lastDotIndex = src.lastIndexOf('.');
                if (lastDotIndex == -1) {
                    throw new IllegalArgumentException("File has no extension: " + src);
                }
                String ext = src.substring(lastDotIndex + 1).toLowerCase();
                if (!availableExtensions.contains(ext)) {
                    throw new IllegalArgumentException("Unsupported file format: " + src);
                }
            }
        }

        return sources;
    }

    public static LocalDateTime parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}. Expected format: yyyy-MM-dd", dateStr);
            throw new IllegalArgumentException("Invalid date format: " + dateStr);
        }
    }

    public static boolean isWithinDateRange(LocalDateTime entryDate, LocalDateTime fromDate, LocalDateTime toDate) {
        if (fromDate != null && entryDate.isBefore(fromDate)) {
            return false;
        }

        return toDate == null || !entryDate.isAfter(toDate.plusDays(1).minusNanos(1));
    }
}
