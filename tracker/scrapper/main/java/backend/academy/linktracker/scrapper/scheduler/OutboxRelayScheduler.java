package backend.academy.linktracker.scrapper.scheduler;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.model.OutboxMessage;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.use-queue", havingValue = "true")
public class OutboxRelayScheduler {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<Long, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        var messages = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        for (OutboxMessage msg : messages) {
            try {
                JsonNode jsonNode = objectMapper.readTree(msg.getPayload());

                LinkUpdateEvent avroEvent = LinkUpdateEvent.newBuilder()
                        .setId(jsonNode.get("id").asLong())
                        .setUrl(jsonNode.get("url").asText())
                        .setDescription(jsonNode.get("description").asText())
                        .setTgChatIds(
                                StreamSupport.stream(jsonNode.get("tgChatIds").spliterator(), false)
                                        .map(JsonNode::asLong)
                                        .toList())
                        .build();

                byte[] avroBytes = serializeToAvro(avroEvent);

                kafkaTemplate.send(msg.getTopic(), Long.parseLong(msg.getKeyValue()), avroBytes);
                msg.setStatus("PROCESSED");
            } catch (Exception e) {
                log.error("Failed to process outbox message {}", msg.getId(), e);
            }
        }
    }

    private byte[] serializeToAvro(LinkUpdateEvent event) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        SpecificDatumWriter<LinkUpdateEvent> writer = new SpecificDatumWriter<>(LinkUpdateEvent.class);
        writer.write(event, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}
