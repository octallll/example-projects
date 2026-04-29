package backend.academy.linktracker.scrapper.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record StackOverflowAnswersResponse(List<AnswerItem> items) {
    public record AnswerItem(
            @JsonProperty("owner") Owner owner,
            @JsonProperty("body") String body,

            @JsonProperty("creation_date") @JsonFormat(shape = JsonFormat.Shape.NUMBER)
            OffsetDateTime creationDate,

            @JsonProperty("answer_id") Long answerId) {
        public record Owner(@JsonProperty("display_name") String displayName) {}
    }
}
