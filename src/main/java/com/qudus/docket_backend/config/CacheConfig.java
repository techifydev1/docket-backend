package com.qudus.docket_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJacksonJsonRedisSerializer jsonRedisSerializer = GenericJacksonJsonRedisSerializer.builder().build();
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)).disableCachingNullValues().serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer));
    }
}
