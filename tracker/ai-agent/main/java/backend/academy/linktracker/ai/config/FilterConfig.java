package backend.academy.linktracker.ai.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "app.filtering")
public class FilterConfig {
    private List<String> stopWords;
    private List<String> excludedAuthors;
    private int minLength;
    private SummarizationConfig summarization;

    @Getter
    @Setter
    public static class SummarizationConfig {
        private int threshold;
    }
}
