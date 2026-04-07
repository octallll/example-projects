package academy.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AnalysisResult(
        @JsonProperty("files") List<String> files,
        @JsonProperty("totalRequestsCount") int totalRequestsCount,
        @JsonProperty("responseSizeInBytes") ResponseSizeStat responseSizeInBytes,
        @JsonProperty("resources") List<ResourceStat> resources,
        @JsonProperty("responseCodes") List<ResponseCodeStat> responseCodes,
        @JsonProperty("requestsPerDate") List<DateStat> requestsPerDate,
        @JsonProperty("uniqueProtocols") List<String> uniqueProtocols) {}
