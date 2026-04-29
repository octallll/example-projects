package backend.academy.linktracker.scrapper.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.IntegrationTest;
import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import backend.academy.linktracker.scrapper.repository.InChatLink;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.LinkService;
import com.github.benmanes.caffeine.cache.Cache;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class LinkServiceCacheTest extends IntegrationTest {
    @Autowired
    private LinkService linkService;

    @MockitoBean
    private LinkRepository linkRepository;

    @Autowired
    private Cache<String, ListLinksResponse> localCache;

    @Autowired
    private RedisTemplate<String, ListLinksResponse> redisTemplate;

    @BeforeEach
    void setUp() {
        localCache.invalidateAll();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shouldHitInCacheOnSecondTry() throws URISyntaxException {
        long chatId = 1L;
        List<InChatLink> mockLinks = List.of(new InChatLink(new URI("https://test.com"), List.of("tag")));

        when(linkRepository.findAllByChatId(chatId)).thenReturn(mockLinks);

        linkService.getByTgChatId(chatId);
        linkService.getByTgChatId(chatId);

        verify(linkRepository, times(1)).findAllByChatId(chatId);
    }

    @Test
    void shouldInvalidateCacheOnAdd() throws URISyntaxException {
        long chatId = 1L;
        URI url = new URI("https://test.com");

        when(linkRepository.findAllByChatId(chatId)).thenReturn(List.of());

        linkService.getByTgChatId(chatId);

        linkService.addLink(chatId, url, List.of());

        linkService.getByTgChatId(chatId);

        verify(linkRepository, times(2)).findAllByChatId(chatId);
    }

    @Test
    void shouldMaintainDataIntegrity() throws URISyntaxException {
        long chatId = 1L;
        URI url = new URI("https://test.com");

        when(linkRepository.findAllByChatId(chatId)).thenReturn(List.of(new InChatLink(url, List.of())));

        ListLinksResponse firstResponse = linkService.getByTgChatId(chatId);

        localCache.invalidateAll();

        ListLinksResponse secondResponse = linkService.getByTgChatId(chatId);

        assertEquals(firstResponse.getSize(), secondResponse.getSize());
        assertEquals(url, secondResponse.getLinks().getFirst().getUrl());
    }
}
