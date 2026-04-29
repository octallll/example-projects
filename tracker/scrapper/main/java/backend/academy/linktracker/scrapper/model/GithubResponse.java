package backend.academy.linktracker.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubResponse(
        String name, @JsonProperty("updated_at") OffsetDateTime updatedAt) {}
