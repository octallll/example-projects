package backend.academy.linktracker.ai.grouping;

import backend.academy.linktracker.ai.config.PrioritizationConfig;
import backend.academy.linktracker.ai.model.Priority;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrioritizationUtils {
    private final PrioritizationConfig prioritizationConfig;

    public Priority getMessagePriority(String text) {
        if (textContains(text, prioritizationConfig.getHighKeywords())) {
            return Priority.HIGH;
        }

        if (textContains(text, prioritizationConfig.getLowKeywords())) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    private boolean textContains(String text, List<String> words) {
        return words.stream().anyMatch(text::contains);
    }

    public Priority maxPriority(Priority firstPriority, Priority secondPriority) {
        return firstPriority.compareTo(secondPriority) < 0 ? firstPriority : secondPriority;
    }
}
