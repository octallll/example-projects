package backend.academy.linktracker.bot;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.bot.service.BotService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
class KafkaProducerTest extends AbstractIntegrationTest {
    @Autowired
    private KafkaTemplate<Long, byte[]> kafkaTemplate;

    @MockitoBean
    private BotService botService;

    @Value("${app.kafka.topic}")
    private String topic;

    @Test
    @DisplayName("Должен успешно доставить сообщение из Scrapper в Bot через Kafka (Avro)")
    void shouldTransferMessageFromScrapperToBot()
            throws ExecutionException, InterruptedException, TimeoutException, IOException {
        LinkUpdateEvent update = LinkUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://github.com/user/repo")
                .setTgChatIds(List.of(123L))
                .setDescription("New update available")
                .build();

        DatumWriter<LinkUpdateEvent> writer = new SpecificDatumWriter<>(LinkUpdateEvent.class);
        byte[] data;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(update, encoder);
            encoder.flush();
            data = out.toByteArray();
        }

        kafkaTemplate.send(topic, update.getId(), data);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> verify(botService, atLeastOnce())
                .sendMessage(anyLong(), contains("New update available")));
    }
}
