package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.dto.ListLinksResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {
    @Bean
    public RedisTemplate<String, ListLinksResponse> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, ListLinksResponse> rt = new RedisTemplate<>();

        rt.setConnectionFactory(cf);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new JacksonJsonRedisSerializer<>(ListLinksResponse.class));

        return rt;
    }

    private static final int MAXIMUM_CACHE_SIZE = 1000;
    private static final Duration LOCAL_CACHE_TTL = Duration.ofMinutes(10);

    @Bean
    public Cache<String, ListLinksResponse> localCacheConfig() {
        return Caffeine.newBuilder()
                .maximumSize(MAXIMUM_CACHE_SIZE)
                .expireAfterWrite(LOCAL_CACHE_TTL)
                .build();
    }
}
