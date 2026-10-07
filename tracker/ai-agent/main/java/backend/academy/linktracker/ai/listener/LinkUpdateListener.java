package backend.academy.linktracker.ai.listener;

import backend.academy.linktracker.ai.filter.FilterUtils;
import backend.academy.linktracker.ai.grouping.GroupingScheduler;
import backend.academy.linktracker.ai.grouping.PrioritizationUtils;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.avro.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkUpdateListener {
    private final GroupingScheduler scheduler;
    private final FilterUtils filterUtils;
    private final PrioritizationUtils prioritizationUtils;

    @RetryableTopic(attempts = "${app.kafka.backoff-attempts}")
    @KafkaListener(topics = "${app.kafka.scrapper-topic}", groupId = "ai-agent-updates-group")
    public void listen(LinkUpdateEvent update) {
        String description = String.valueOf(update.getDescription());
        String author = String.valueOf(update.getAuthor());

        if (!filterUtils.validateMessage(description, author)) {
            return;
        }

        Priority priority = prioritizationUtils.getMessagePriority(description);

        String summaryDescriptions = filterUtils.summarizeMessage(description);

        scheduler.processUpdate(summaryDescriptions, priority, update.getTgChatIds());
    }

    @DltHandler
    public void handleDlt(LinkUpdateEvent update, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Update {} sent to DLT after failed retries from topic {}", update.getId(), topic);
    }
}
