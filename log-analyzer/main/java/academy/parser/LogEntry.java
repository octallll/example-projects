package academy.parser;

import java.time.LocalDateTime;

public record LogEntry(
        String ip,
        String user,
        LocalDateTime localDateTime,
        String method,
        String resource,
        String protocol,
        int statusCode,
        int responseSize,
        String refer,
        String userAgent) {}
