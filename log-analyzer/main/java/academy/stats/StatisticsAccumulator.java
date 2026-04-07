package academy.stats;

import academy.parser.LogEntry;
import com.tdunning.math.stats.TDigest;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StatisticsAccumulator {
    private int totalRequestsCount = 0;
    private long totalResponseSize = 0;

    private final List<Integer> responseSizes = new ArrayList<>();
    private final Map<Integer, Integer> responseCodes = new HashMap<>();
    private final Map<String, Integer> resources = new HashMap<>();
    private final Map<LocalDate, Integer> requestsPerDate = new HashMap<>();
    private final Set<String> uniqueProtocols = new LinkedHashSet<>();
    private final TDigest responseSizeDigest = TDigest.createAvlTreeDigest(100);

    private final DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public void update(LogEntry entry) {
        totalRequestsCount++;

        totalResponseSize += entry.responseSize();
        responseSizes.add(entry.responseSize());
        responseSizeDigest.add(entry.responseSize());

        responseCodes.put(entry.statusCode(), responseCodes.getOrDefault(entry.statusCode(), 0) + 1);

        resources.put(entry.resource(), resources.getOrDefault(entry.resource(), 0) + 1);

        LocalDate date = entry.localDateTime().toLocalDate();
        requestsPerDate.put(date, requestsPerDate.getOrDefault(date, 0) + 1);

        uniqueProtocols.add(entry.protocol());
    }

    public AnalysisResult buildResult(List<String> files) {
        Collections.sort(responseSizes);

        int maxResponseSize = responseSizes.isEmpty() ? 0 : responseSizes.getLast();

        double rawAverage = totalRequestsCount > 0 ? (double) totalResponseSize / totalRequestsCount : 0.0;

        double averageResponseSize = Math.round(rawAverage * 100.0) / 100.0;

        double p95ResponseSize = 0.0;
        if (totalRequestsCount > 0) {
            p95ResponseSize = responseSizeDigest.quantile(0.95);
            p95ResponseSize = Math.round(p95ResponseSize);
        }

        List<ResourceStat> topResources = resources.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new ResourceStat(entry.getKey(), entry.getValue()))
                .toList();

        List<ResponseCodeStat> responseCodeStats = responseCodes.entrySet().stream()
                .map(entry -> new ResponseCodeStat(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Integer.compare(b.totalResponsesCount(), a.totalResponsesCount()))
                .toList();

        List<DateStat> dateStats = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : requestsPerDate.entrySet()) {
            LocalDate date = entry.getKey();
            int count = entry.getValue();

            double percentage =
                    totalRequestsCount > 0 ? Math.round(count * 100.0 / totalRequestsCount * 100.0) / 100.0 : 0.0;

            String weekday = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            dateStats.add(new DateStat(date.format(isoDateFormatter), weekday, count, percentage));
        }

        dateStats.sort(Comparator.comparing(DateStat::date));

        List<String> fileNames = files.stream()
                .map(src -> {
                    if (src.startsWith("http://") || src.startsWith("https://")) {
                        return src;
                    }

                    try {
                        return Path.of(src).getFileName().toString();
                    } catch (InvalidPathException e) {
                        return src;
                    }
                })
                .sorted()
                .toList();

        return new AnalysisResult(
                fileNames,
                totalRequestsCount,
                new ResponseSizeStat(averageResponseSize, maxResponseSize, p95ResponseSize),
                topResources,
                responseCodeStats,
                dateStats,
                uniqueProtocols.stream().toList());
    }

    public int getTotalRequestsCount() {
        return totalRequestsCount;
    }
}
