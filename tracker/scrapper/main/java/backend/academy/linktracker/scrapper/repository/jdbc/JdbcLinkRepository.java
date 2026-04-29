package backend.academy.linktracker.scrapper.repository.jdbc;

import backend.academy.linktracker.scrapper.model.Link;
import backend.academy.linktracker.scrapper.repository.InChatLink;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "sql")
public class JdbcLinkRepository implements LinkRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addChat(Long chatId) {
        jdbcTemplate.update("INSERT INTO chats (id) VALUES (?) ON CONFLICT DO NOTHING", chatId);
    }

    @Override
    public void removeChat(Long chatId) {
        jdbcTemplate.update("DELETE FROM chats WHERE id = ?", chatId);
    }

    @Override
    public boolean existsChat(Long chatId) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM chats WHERE id = ?", Integer.class, chatId);

        return count != null && count > 0;
    }

    @Override
    public void add(Long chatId, URI url, List<String> tags) {
        addChat(chatId);

        jdbcTemplate.update("INSERT INTO links (url) VALUES (?) ON CONFLICT DO NOTHING", url.toString());

        Integer linkId = getLinkIdByUrl(url);

        String[] tagsArray = tags.toArray(new String[0]);

        jdbcTemplate.update(
                "INSERT INTO subscriptions (chat_id, link_id, tags) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                chatId,
                linkId,
                tagsArray);
    }

    @Override
    public void remove(URI url) {
        jdbcTemplate.update("DELETE FROM links WHERE url = ?", url.toString());
    }

    @Override
    public List<Link> getOldestLinks(Duration interval) {
        return getOldestLinks(interval, Integer.MAX_VALUE);
    }

    @Override
    public List<Link> getOldestLinks(Duration interval, int limit) {
        OffsetDateTime threshold = OffsetDateTime.now().minus(interval);
        return jdbcTemplate.query(
                "SELECT * FROM links WHERE last_check_at < ? OR last_check_at IS NULL LIMIT ?",
                linkMapper,
                threshold,
                limit);
    }

    private final RowMapper<Link> linkMapper = (rs, _) -> {
        try {
            return new Link(
                    rs.getLong("id"),
                    URI.create(rs.getString("url")),
                    rs.getObject("last_update_time", OffsetDateTime.class),
                    rs.getObject("last_check_at", OffsetDateTime.class));
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping Link from ResultSet", e);
        }
    };

    @Override
    public void update(Link link) {
        jdbcTemplate.update(
                "UPDATE links SET last_update_time = ?, last_check_at = ? WHERE id = ?",
                link.getLastUpdateTime(),
                link.getLastCheck(),
                link.getId());
    }

    @Override
    public List<InChatLink> findAllByChatId(Long chatId) {
        List<Integer> linkIds =
                jdbcTemplate.queryForList("SELECT link_id FROM subscriptions WHERE chat_id = ?", Integer.class, chatId);

        if (linkIds.isEmpty()) {
            return List.of();
        }

        List<InChatLink> result = new ArrayList<>();

        for (Integer linkId : linkIds) {
            String url = jdbcTemplate.queryForObject("SELECT url FROM links WHERE id = ?", String.class, linkId);

            String[] tagsArray = jdbcTemplate.queryForObject(
                    "SELECT tags FROM subscriptions WHERE chat_id = ? AND link_id = ?",
                    (rs, _) -> {
                        java.sql.Array sqlArray = rs.getArray("tags");
                        return sqlArray != null ? (String[]) sqlArray.getArray() : new String[0];
                    },
                    chatId,
                    linkId);

            List<String> tags = tagsArray != null ? Arrays.asList(tagsArray) : List.of();

            assert url != null;
            result.add(new InChatLink(URI.create(url), tags));
        }

        return result;
    }

    @Override
    public void add(URI url) {
        jdbcTemplate.update("INSERT INTO links (url) VALUES (?)", url.toString());
    }

    @Override
    public void remove(Long chatId, URI url) {
        Integer linkId = getLinkIdByUrl(url);

        jdbcTemplate.update("DELETE FROM subscriptions WHERE chat_id = ? AND link_id = ?", chatId, linkId);
    }

    @Override
    public List<Long> findByUrl(URI url) {
        return jdbcTemplate.queryForList(
                "SELECT chat_id FROM subscriptions WHERE link_id = ?", Long.class, getLinkIdByUrl(url));
    }

    private Integer getLinkIdByUrl(URI url) {
        return jdbcTemplate.queryForObject("SELECT id FROM links WHERE url = ?", Integer.class, url.toString());
    }
}
