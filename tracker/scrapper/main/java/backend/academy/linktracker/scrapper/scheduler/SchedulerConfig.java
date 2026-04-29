package backend.academy.linktracker.scrapper.scheduler;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduler")
public record SchedulerConfig(int batchSize, Duration interval, Duration checkInterval) {}
