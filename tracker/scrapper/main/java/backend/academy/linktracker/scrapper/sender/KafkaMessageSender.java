package backend.academy.linktracker.scrapper.sender;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.use-queue", havingValue = "without-outbox")
public class KafkaMessageSender implements MessageSender {
    private final LinkRepository linkRepository;
    private final KafkaTemplate<Long, LinkUpdateEvent> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topicName;

    @Override
    public void sendUpdate(Link link, String reason) {
        LinkUpdateEvent updateEvent = LinkUpdateEvent.newBuilder()
                .setId(link.getId())
                .setUrl(link.getUrl().toString())
                .setTgChatIds(linkRepository.findByUrl(link.getUrl()))
                .setDescription(reason)
                .build();

        kafkaTemplate.send(topicName, updateEvent.getId(), updateEvent);
    }
}
