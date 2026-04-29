package backend.academy.linktracker.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Link {
    private Long id;
    private URI url;
    private OffsetDateTime lastUpdateTime;
    private OffsetDateTime lastCheck;
}
