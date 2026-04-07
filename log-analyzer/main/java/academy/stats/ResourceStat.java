package academy.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceStat(
        @JsonProperty("resource") String resource, @JsonProperty("totalRequestsCount") int totalRequestsCount) {}
