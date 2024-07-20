package com.blogging.dataprovider.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory,
                                          final RedisTemplate<String, Object> redisTemplate,
                                          final ObjectMapper objectMapper) {
        CustomRedisCacheWriter cacheWriter = new CustomRedisCacheWriter(connectionFactory,
                redisTemplate, new GenericJackson2JsonRedisSerializer(objectMapper));
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
        return RedisCacheManager.builder(cacheWriter).cacheDefaults(config).build();
    }

    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> target.getClass().getSimpleName() + ":" + method.getName() + ":" +
                Arrays.stream(params).map(Object::toString).reduce((a, b) -> a + ":" + b).orElse("");
    }

    @Bean
    public RedisSerializer<Object> redisSerializer(ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheErrorHandler customCacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
            }
        };
    }

}
