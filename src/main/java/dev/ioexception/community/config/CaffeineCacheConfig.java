package dev.ioexception.community.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    @Bean
    public CaffeineCacheManager cacheConfig() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager("topKeyword");

        caffeineCacheManager.setCaffeine(caffeineCacheBuilder());

        return caffeineCacheManager;
    }

    Caffeine<Object, Object> caffeineCacheBuilder() {

        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(100);
    }
}
