package backend.academy.linktracker.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubIssueResponse(
        String title,
        String body,
        @JsonProperty("html_url") String htmlUrl,
        User user,
        @JsonProperty("created_at") OffsetDateTime createdAt) {
    public record User(String login) {}
}
