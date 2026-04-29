package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.InChatLink;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import com.github.benmanes.caffeine.cache.Cache;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LinkService {
    private final LinkRepository linkRepository;
    private final Cache<String, ListLinksResponse> localCache;
    private final RedisTemplate<String, ListLinksResponse> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "tg-chat-id:";

    @Value("${app.redis.ttl}")
    private Duration TTL;

    public ListLinksResponse getByTgChatId(Long tgChatId) {
        String cacheKey = CACHE_KEY_PREFIX + tgChatId;

        ListLinksResponse localCached = localCache.getIfPresent(cacheKey);

        if (localCached != null) {
            log.atInfo().addKeyValue("key", tgChatId).log("Found cached value");

            return localCached;
        }

        ListLinksResponse cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.atInfo().addKeyValue("key", tgChatId).log("Found cached value");

            return cached;
        }

        List<InChatLink> links = linkRepository.findAllByChatId(tgChatId);
        List<LinkResponse> responseList =
                links.stream().map(this::mapToResponse).toList();
        ListLinksResponse response = new ListLinksResponse().links(responseList).size(responseList.size());

        redisTemplate.opsForValue().set(cacheKey, response, TTL);

        return response;
    }

    public void addLink(Long tgChatId, URI url, List<String> tags) {
        localCache.invalidate(CACHE_KEY_PREFIX + tgChatId);
        redisTemplate.delete(CACHE_KEY_PREFIX + tgChatId);
        linkRepository.add(tgChatId, url, tags);
    }

    public void removeLink(Long tgChatId, URI link) {
        localCache.invalidate(CACHE_KEY_PREFIX + tgChatId);
        redisTemplate.delete(CACHE_KEY_PREFIX + tgChatId);
        linkRepository.remove(tgChatId, link);
    }

    private LinkResponse mapToResponse(InChatLink inChatLink) {
        return new LinkResponse().url(inChatLink.url()).tags(inChatLink.tags());
    }
}
