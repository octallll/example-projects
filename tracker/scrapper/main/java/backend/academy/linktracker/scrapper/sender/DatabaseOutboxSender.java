package backend.academy.linktracker.scrapper.sender;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.OutboxMessage;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.use-queue", havingValue = "true")
public class DatabaseOutboxSender implements MessageSender {
    private final LinkRepository linkRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic}")
    private String topicName;

    @Override
    @Transactional
    @SneakyThrows
    public void sendUpdate(Link link, String reason) {
        Map<String, Object> payload = Map.of(
                "id", link.getId(),
                "url", link.getUrl().toString(),
                "description", reason,
                "tgChatIds", linkRepository.findByUrl(link.getUrl()));

        OutboxMessage outbox = new OutboxMessage();
        outbox.setId(UUID.randomUUID());
        outbox.setTopic(topicName);
        outbox.setKeyValue(link.getId().toString());
        outbox.setPayload(objectMapper.writeValueAsString(payload));
        outbox.setStatus("PENDING");

        outboxRepository.save(outbox);
    }
}
