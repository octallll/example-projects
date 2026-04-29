package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.model.Link;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public interface LinkRepository {
    void addChat(Long chatId);

    void removeChat(Long chatId);

    boolean existsChat(Long chatId);

    void add(Long chatId, URI url, List<String> tags);

    void remove(URI url);

    List<Link> getOldestLinks(Duration interval);

    List<Link> getOldestLinks(Duration interval, int limit);

    void update(Link link);

    List<InChatLink> findAllByChatId(Long chatId);

    void add(URI url);

    void remove(Long chatId, URI url);

    List<Long> findByUrl(URI url);
}
