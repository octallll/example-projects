package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "in-memory")
public class InMemoryLinkRepository implements LinkRepository {
    private final ConcurrentHashMap<Long, Set<InChatLink>> linksByChat = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<URI, Link> links = new ConcurrentHashMap<>();

    private final AtomicLong currentUnusedId = new AtomicLong(0);

    @Override
    public void addChat(Long chatId) {
        linksByChat.putIfAbsent(chatId, new HashSet<>());
    }

    @Override
    public void removeChat(Long chatId) {
        linksByChat.remove(chatId);
    }

    @Override
    public boolean existsChat(Long chatId) {
        return linksByChat.containsKey(chatId);
    }

    @Override
    public void add(Long chatId, URI url, List<String> tags) {
        log.info("Add to {}, url: {}", chatId, url);

        addChat(chatId);
        linksByChat.get(chatId).add(new InChatLink(url, tags));

        links.putIfAbsent(
                url, new Link(currentUnusedId.getAndIncrement(), url, OffsetDateTime.now(), OffsetDateTime.now()));
    }

    @Override
    public void remove(URI url) {
        links.remove(url);
    }

    @Override
    public List<Link> getOldestLinks(Duration interval) {
        return getOldestLinks(interval, Integer.MAX_VALUE);
    }

    @Override
    public List<Link> getOldestLinks(Duration interval, int limit) {
        OffsetDateTime needTime = OffsetDateTime.now().minus(interval);

        return links.values().stream()
                .filter(link -> link.getLastCheck().isBefore(needTime))
                .limit(limit)
                .toList();
    }

    @Override
    public void update(Link link) {
        links.put(link.getUrl(), link);
    }

    @Override
    public List<InChatLink> findAllByChatId(Long chatId) {
        Set<InChatLink> links = linksByChat.get(chatId);

        if (links == null) {
            return Collections.emptyList();
        }

        return links.stream().toList();
    }

    @Override
    public void add(URI url) {
        links.putIfAbsent(
                url, new Link(currentUnusedId.getAndIncrement(), url, OffsetDateTime.now(), OffsetDateTime.now()));
    }

    @Override
    public void remove(Long chatId, URI url) {
        Set<InChatLink> chatLinks = linksByChat.get(chatId);
        if (chatLinks != null) {
            chatLinks.removeIf(link -> link.url().equals(url));
        }
    }

    @Override
    public List<Long> findByUrl(URI url) {
        return linksByChat.entrySet().stream()
                .filter(entry ->
                        entry.getValue().stream().anyMatch(link -> link.url().equals(url)))
                .map(Map.Entry::getKey)
                .toList();
    }
}
