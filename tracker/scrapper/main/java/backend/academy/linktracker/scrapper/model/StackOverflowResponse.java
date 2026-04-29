package backend.academy.linktracker.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowResponse(List<StackOverflowItem> items) {
    public record StackOverflowItem(
            @JsonProperty("question_id") Long questionId,
            String title,
            @JsonProperty("last_activity_date") OffsetDateTime lastActivityDate) {}
}
