package com.blogging.dataprovider.cache;

import com.blogging.dataprovider.cache.writer.CustomRedisCacheWriter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory,
                                          final RedisTemplate<String, Object> redisTemplate,
                                          final ObjectMapper objectMapper) {
        var serializationMapper = createSerializationMapper(objectMapper);

        CustomRedisCacheWriter cacheWriter = createCustomCacheWriter(connectionFactory, redisTemplate, serializationMapper);
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(serializationMapper)))
                .entryTtl(Duration.ofMinutes(1));
        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(config)
                .build();
    }

    private static CustomRedisCacheWriter createCustomCacheWriter(final RedisConnectionFactory connectionFactory,
                                                                  final RedisTemplate<String, Object> redisTemplate,
                                                                  final ObjectMapper serializationMapper) {
        return new CustomRedisCacheWriter(connectionFactory,
                redisTemplate, new GenericJackson2JsonRedisSerializer(serializationMapper));
    }

    private static ObjectMapper createSerializationMapper(final ObjectMapper objectMapper) {
        var serializationMapper = objectMapper.copy();
        serializationMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        serializationMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        return serializationMapper;
    }

    @Bean("customKeyGenerator")
    public KeyGenerator customKeyGenerator(){
        return new CustomCacheKeyGenerator();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
