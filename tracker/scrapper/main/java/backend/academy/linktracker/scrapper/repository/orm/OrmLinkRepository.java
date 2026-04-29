package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.repository.InChatLink;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.orm.jpa.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.orm.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.orm.jpa.JpaSubscriptionRepository;
import backend.academy.linktracker.scrapper.repository.orm.tables.Chat;
import backend.academy.linktracker.scrapper.repository.orm.tables.Link;
import backend.academy.linktracker.scrapper.repository.orm.tables.Subscription;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "orm")
@RequiredArgsConstructor
@Transactional
public class OrmLinkRepository implements LinkRepository {

    private final JpaChatRepository chatRepository;
    private final JpaLinkRepository linkRepository;
    private final JpaSubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void addChat(Long chatId) {
        if (!chatRepository.existsById(chatId)) {
            chatRepository.save(getChat(chatId));
        }
    }

    @Override
    public void removeChat(Long chatId) {
        chatRepository.delete(getChat(chatId));
    }

    @Override
    public boolean existsChat(Long chatId) {
        return chatRepository.existsById(chatId);
    }

    @Override
    public void add(Long chatId, URI url, List<String> tags) {
        addChat(chatId);

        Link link = linkRepository.findByUrl(url.toString()).orElseGet(() -> {
            Link newLink = new Link();
            newLink.setUrl(url.toString());
            return linkRepository.save(newLink);
        });

        Subscription sub = new Subscription();
        sub.setChatId(chatRepository.getReferenceById(chatId));
        sub.setLinkId(link);
        sub.setTags(tags);

        subscriptionRepository.save(sub);
    }

    @Override
    public void remove(URI url) {
        linkRepository.findByUrl(url.toString()).ifPresent(linkRepository::delete);
    }

    @Override
    public List<backend.academy.linktracker.scrapper.model.Link> getOldestLinks(Duration interval) {
        return getOldestLinks(interval, Integer.MAX_VALUE);
    }

    @Override
    public List<backend.academy.linktracker.scrapper.model.Link> getOldestLinks(Duration interval, int limit) {
        OffsetDateTime threshold = OffsetDateTime.now().minus(interval);

        List<Link> entities = linkRepository.findOldestLinks(threshold);

        return entities.stream()
                .limit(limit)
                .map(entity -> new backend.academy.linktracker.scrapper.model.Link(
                        entity.getId(),
                        URI.create(entity.getUrl()),
                        entity.getLastUpdateTime(),
                        entity.getLastCheckAt()))
                .toList();
    }

    @Override
    public void update(backend.academy.linktracker.scrapper.model.Link link) {
        Link entity = linkRepository
                .findById(link.getId())
                .orElseThrow(() -> new EntityNotFoundException("Link not found with id: " + link.getId()));

        entity.setLastUpdateTime(link.getLastUpdateTime());
        entity.setLastCheckAt(link.getLastCheck());
        entity.setUrl(link.getUrl().toString());
    }

    @Override
    public List<InChatLink> findAllByChatId(Long chatId) {
        return chatRepository
                .findById(chatId)
                .map(value -> subscriptionRepository.findAllByChatId(value).stream()
                        .map(subscription -> {
                            try {
                                return new InChatLink(
                                        new URI(subscription.getLinkId().getUrl()), subscription.getTags());
                            } catch (URISyntaxException e) {
                                throw new RuntimeException("Error while mapping url: " + e.getMessage());
                            }
                        })
                        .toList())
                .orElseGet(List::of);
    }

    @Override
    public void add(URI url) {
        Link newLink = new Link();
        newLink.setUrl(url.toString());
        linkRepository.save(newLink);
    }

    @Override
    public void remove(Long chatId, URI url) {
        Optional<Chat> chat = chatRepository.findById(chatId);

        if (chat.isEmpty()) {
            return;
        }

        subscriptionRepository.delete(subscriptionRepository
                .findByChatIdAndLinkId_Url(
                        chat.orElseThrow(() -> new EntityNotFoundException("Subscription not found")), url.toString())
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found")));
    }

    @Override
    public List<Long> findByUrl(URI url) {
        return subscriptionRepository.findAllByLinkId_Url(url.toString()).stream()
                .map(subscription -> subscription.getLinkId().getId())
                .toList();
    }

    private Chat getChat(Long chatId) {
        Chat chat = new Chat();
        chat.setId(chatId);

        return chat;
    }
}
