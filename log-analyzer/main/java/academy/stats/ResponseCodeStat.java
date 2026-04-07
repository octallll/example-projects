package academy.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseCodeStat(
        @JsonProperty("code") int code, @JsonProperty("totalResponsesCount") int totalResponsesCount) {}
