package academy.stats;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DateStat(
        @JsonProperty("date") String date,
        @JsonProperty("weekday") String weekday,
        @JsonProperty("totalRequestsCount") int totalRequestsCount,
        @JsonProperty("totalRequestsPercentage") double totalRequestsPercentage) {}
