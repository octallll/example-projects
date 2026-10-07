package backend.academy.linktracker.ai;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.config.PrioritizationConfig;
import backend.academy.linktracker.ai.grouping.GroupingScheduler;
import backend.academy.linktracker.ai.sender.MessageSender;
import backend.academy.linktracker.avro.LinkUpdateEvent;
import backend.academy.linktracker.avro.PrioritizationLinkUpdateEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {"app.prioritization.grouping.window-ms=2000"})
public class KafkaAIAgentTest extends TestcontainersConfiguration {
    @Autowired
    private KafkaTemplate<Long, byte[]> kafkaTemplate;

    @Autowired
    private GroupingScheduler groupingScheduler;

    @MockitoBean
    private ChatModel chatModel;

    @MockitoBean
    private PrioritizationConfig prioritizationConfig;

    @MockitoBean
    private MessageSender messageSender;

    @Value("${app.kafka.scrapper-topic}")
    private String scrapperTopic;

    @Test
    void testProcessMessage() throws IOException {
        mockAiResponse("AI Summary");

        LinkUpdateEvent update = LinkUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://github.com/test/repo")
                .setAuthor("author")
                .setDescription("Very long description that needs to be summarized by AI...")
                .setTgChatIds(List.of(123L))
                .build();

        kafkaTemplate.send(scrapperTopic, update.getId(), serialize(update));

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    groupingScheduler.process();
                    verify(messageSender).sendUpdate(any(PrioritizationLinkUpdateEvent.class));
                });
    }

    @Test
    void shouldSendSingleUpdateWithoutGroupingChanges() throws IOException {
        when(prioritizationConfig.getHighKeywords()).thenReturn(List.of("critical", "crash"));
        when(prioritizationConfig.getLowKeywords()).thenReturn(List.of("typo"));

        mockAiResponse("Summary text");

        LinkUpdateEvent update = LinkUpdateEvent.newBuilder()
                .setId(3L)
                .setUrl("https://github.com/test/repo")
                .setAuthor("author")
                .setDescription("What lamo a da sdfaafaad ")
                .setTgChatIds(List.of(456L))
                .build();

        kafkaTemplate.send(scrapperTopic, update.getId(), serialize(update));

        ArgumentCaptor<PrioritizationLinkUpdateEvent> captor =
                ArgumentCaptor.forClass(PrioritizationLinkUpdateEvent.class);

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    groupingScheduler.process();
                    verify(messageSender).sendUpdate(captor.capture());
                });

        PrioritizationLinkUpdateEvent capturedEvent = captor.getValue();

        assertEquals("What lamo a da sdfaafaad ", capturedEvent.getDescription().toString());
        assertEquals(List.of(456L), capturedEvent.getTgChatIds());
        assertEquals("MEDIUM", capturedEvent.getPriority());
    }

    @Test
    void testManyEvents() throws IOException {
        when(prioritizationConfig.getHighKeywords()).thenReturn(List.of("critical", "crash"));
        when(prioritizationConfig.getLowKeywords()).thenReturn(List.of("typo"));

        mockAiResponse("Summary text");

        LinkUpdateEvent update = LinkUpdateEvent.newBuilder()
                .setId(3L)
                .setUrl("https://github.com/test/repo")
                .setAuthor("author")
                .setDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa critical")
                .setTgChatIds(List.of(456L))
                .build();

        kafkaTemplate.send(scrapperTopic, update.getId(), serialize(update));

        LinkUpdateEvent update1 = LinkUpdateEvent.newBuilder()
                .setId(3L)
                .setUrl("https://github.com/test/repo")
                .setAuthor("author")
                .setDescription("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb typo")
                .setTgChatIds(List.of(456L))
                .build();

        kafkaTemplate.send(scrapperTopic, update1.getId(), serialize(update1));

        ArgumentCaptor<PrioritizationLinkUpdateEvent> captor =
                ArgumentCaptor.forClass(PrioritizationLinkUpdateEvent.class);

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    groupingScheduler.process();
                    verify(messageSender).sendUpdate(captor.capture());
                });

        PrioritizationLinkUpdateEvent capturedEvent = captor.getValue();

        assertEquals(
                "1 aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa critical\n2 bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb typo",
                capturedEvent.getDescription().toString());
        assertEquals(List.of(456L), capturedEvent.getTgChatIds());
        assertEquals("HIGH", capturedEvent.getPriority());
    }

    private void mockAiResponse(String text) {
        ChatResponse response = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        DeepSeekAssistantMessage message = mock(DeepSeekAssistantMessage.class);

        when(chatModel.call(any(org.springframework.ai.chat.prompt.Prompt.class)))
                .thenReturn(response);

        when(response.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        when(message.getText()).thenReturn(text);
    }

    private byte[] serialize(LinkUpdateEvent event) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        new SpecificDatumWriter<>(LinkUpdateEvent.class).write(event, encoder);
        encoder.flush();
        return out.toByteArray();
    }
}
