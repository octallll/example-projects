package backend.academy.linktracker.ai.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "app.prioritization")
public class PrioritizationConfig {
    private List<String> highKeywords;
    private List<String> lowKeywords;
    private GroupingConfig grouping;

    @Getter
    @Setter
    private static class GroupingConfig {
        private int windowMs;
    }
}
