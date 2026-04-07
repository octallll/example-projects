package academy.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseSizeStat(
        @JsonProperty("average") double average, @JsonProperty("max") double max, @JsonProperty("p95") double p95) {}
