package backend.academy.linktracker.scrapper.kafka;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.linktracker.scrapper.IntegrationTest;
import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.model.OutboxMessage;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.OutboxRepository;
import backend.academy.linktracker.scrapper.sender.DatabaseOutboxSender;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest
public class DatabaseOutboxSenderTest extends IntegrationTest {
    @Autowired
    private DatabaseOutboxSender outboxSender;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private LinkRepository linkRepository;

    @BeforeEach
    void setOutboxRepository() {
        outboxRepository.deleteAllInBatch();
    }

    @Test
    void sendUpdate_ShouldSaveMessageToDatabase() throws URISyntaxException {
        Link link = new Link(123L, new URI("https://e1.ru"), OffsetDateTime.now(), OffsetDateTime.now());

        linkRepository.add(1L, new URI("https://e1.ru"), List.of("tag"));

        outboxSender.sendUpdate(link, "new update");

        List<OutboxMessage> messages = outboxRepository.findAll();
        assertThat(messages).hasSize(1);

        OutboxMessage captured = messages.getFirst();
        assertThat(captured.getStatus()).isEqualTo("PENDING");
        assertThat(captured.getTopic()).isEqualTo("test-topic");
        assertThat(captured.getPayload()).contains("https://e1.ru");
    }

    @Test
    void relay_ShouldProcessMessageFromDatabase() throws URISyntaxException {
        Link link = new Link(456L, new URI("test.com"), OffsetDateTime.now(), OffsetDateTime.now());

        linkRepository.add(2L, new URI("test.com"), List.of("test"));

        outboxSender.sendUpdate(link, "relay test");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var messages = outboxRepository.findAll();
                    assertThat(messages).isNotEmpty();
                    assertThat(messages.getFirst().getStatus()).isEqualTo("PROCESSED");
                });
    }
}
