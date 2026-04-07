package academy.writers;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractWriter implements Writer {
    protected BufferedWriter writer;

    static final Map<Integer, String> statusDescriptions = new HashMap<>();

    static {
        statusDescriptions.put(100, "Continue");
        statusDescriptions.put(101, "Switching Protocols");
        statusDescriptions.put(200, "OK");
        statusDescriptions.put(201, "Created");
        statusDescriptions.put(202, "Accepted");
        statusDescriptions.put(204, "No Content");
        statusDescriptions.put(301, "Moved Permanently");
        statusDescriptions.put(302, "Found");
        statusDescriptions.put(304, "Not Modified");
        statusDescriptions.put(400, "Bad Request");
        statusDescriptions.put(401, "Unauthorized");
        statusDescriptions.put(403, "Forbidden");
        statusDescriptions.put(404, "Not Found");
        statusDescriptions.put(405, "Method Not Allowed");
        statusDescriptions.put(500, "Internal Server Error");
        statusDescriptions.put(502, "Bad Gateway");
        statusDescriptions.put(503, "Service Unavailable");
        statusDescriptions.put(504, "Gateway Timeout");
    }

    AbstractWriter(BufferedWriter writer) {
        this.writer = writer;
    }
}
