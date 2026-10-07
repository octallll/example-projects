package backend.academy.linktracker.ai;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.config.PrioritizationConfig;
import backend.academy.linktracker.ai.grouping.PrioritizationUtils;
import backend.academy.linktracker.ai.model.Priority;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class PrioritizationTest extends TestcontainersConfiguration {
    @Autowired
    private PrioritizationUtils prioritizationUtils;

    @MockitoBean
    private PrioritizationConfig prioritizationConfig;

    @Test
    public void testHighPriority() {
        when(prioritizationConfig.getHighKeywords()).thenReturn(List.of("critical", "crash"));
        when(prioritizationConfig.getLowKeywords()).thenReturn(List.of("typo"));

        String description = "critical bug";

        assertSame(Priority.HIGH, prioritizationUtils.getMessagePriority(description));
    }

    @Test
    public void testLowPriority() {
        when(prioritizationConfig.getHighKeywords()).thenReturn(List.of("critical", "crash"));
        when(prioritizationConfig.getLowKeywords()).thenReturn(List.of("typo"));

        String description = "typo in code";

        assertSame(Priority.LOW, prioritizationUtils.getMessagePriority(description));
    }

    @Test
    public void testMediumPriority() {
        when(prioritizationConfig.getHighKeywords()).thenReturn(List.of("critical", "crash"));
        when(prioritizationConfig.getLowKeywords()).thenReturn(List.of("typo"));

        String description = "update on github";

        assertSame(Priority.MEDIUM, prioritizationUtils.getMessagePriority(description));
    }
}
