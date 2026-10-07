package backend.academy.linktracker.ai.sender;

import backend.academy.linktracker.avro.PrioritizationLinkUpdateEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageSender implements MessageSender {
    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Value("${app.kafka.bot-topic}")
    private String topicName;

    @Override
    public void sendUpdate(PrioritizationLinkUpdateEvent event) {
        try {
            kafkaTemplate.send(topicName, event.getId(), serializeToAvro(event));
        } catch (IOException e) {
            log.atError().addKeyValue("event", event).setCause(e).log("Serialization error");
        }
    }

    private byte[] serializeToAvro(PrioritizationLinkUpdateEvent event) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        SpecificDatumWriter<PrioritizationLinkUpdateEvent> writer =
                new SpecificDatumWriter<>(PrioritizationLinkUpdateEvent.class);
        writer.write(event, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}
