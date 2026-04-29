package backend.academy.linktracker.scrapper.db;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.linktracker.scrapper.IntegrationTest;
import backend.academy.linktracker.scrapper.repository.InChatLink;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.util.List;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractLinkRepositoryTest extends IntegrationTest {
    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    protected LinkRepository repository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Test
    void add_LinkIsStoredInDatabase() {
        Long chatId = 1L;
        URI url = URI.create("https://github.com/sveshnikov");
        List<String> tags = List.of("java", "test");

        repository.add(chatId, url, tags);

        List<InChatLink> result = repository.findAllByChatId(chatId);
        assertThat(result).hasSize(1);
        AssertionsForClassTypes.assertThat(result.getFirst().url()).isEqualTo(url);
    }

    @Test
    void remove_LinkIsRemovedFromDatabase() {
        URI url = URI.create("https://google.com");
        repository.add(url);

        repository.remove(url);

        Integer count =
                jdbcTemplate.queryForObject("SELECT count(*) FROM links WHERE url = ?", Integer.class, url.toString());
        AssertionsForClassTypes.assertThat(count).isZero();
    }

    @Test
    void migrations_ShouldApplySuccessfullyOnStartup() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'links'", Integer.class);

        AssertionsForClassTypes.assertThat(count).isEqualTo(1);
    }
}
