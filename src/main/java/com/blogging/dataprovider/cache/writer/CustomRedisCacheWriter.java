package com.blogging.dataprovider.cache.writer;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CustomRedisCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter delegate;
    private final GenericJackson2JsonRedisSerializer serializer;
    private final CustomRedisCacheWriterHelper helper;

    public CustomRedisCacheWriter(RedisConnectionFactory connectionFactory,
                                  RedisTemplate<String, Object> redisTemplate,
                                  GenericJackson2JsonRedisSerializer serializer) {
        this.delegate = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        this.helper = new CustomRedisCacheWriterHelper(redisTemplate);
        this.serializer = serializer;
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        String stringKey = helper.processKey(name, key);
        Object deserializedValue = serializer.deserialize(value);

        switch (deserializedValue) {
            case Map<?, ?> map -> helper.putMap(stringKey, map);
            case List<?> list -> helper.putList(stringKey, list);
            case null -> throw new IllegalArgumentException("Serialization produced null value");
            default -> helper.putSingle(stringKey, deserializedValue);
        }
    }

    @Override
    public byte[] get(String name, byte[] key) {
        String stringKey = helper.processKey(name, key);
        String valueType = CustomRedisCacheWriterHelper.extractValueType(stringKey);
        List<String> ids = CustomRedisCacheWriterHelper.extractIds(stringKey);

        List<Object> redisResult = helper.fetchFromRedis(stringKey, ids);
        if (redisResult == null) {
            return null;
        }

        return switch (valueType) {
            case "map" -> serializer.serialize(helper.createResultMap(ids, redisResult));
            case "list" -> serializer.serialize(redisResult);
            case "obj" -> serializer.serialize(redisResult.getFirst());
            default -> throw new IllegalArgumentException("Unsupported value type: " + valueType);
        };
    }

    // Delegate other methods or implement as needed

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.putIfAbsent(name, key, value, ttl);
    }

    @Override
    public void remove(String name, byte[] key) {
        delegate.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        delegate.clean(name, pattern);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return delegate.getCacheStatistics(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector) {
        return delegate.withStatisticsCollector(cacheStatisticsCollector);
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, Duration ttl) {
        return delegate.store(name, key, value, ttl);
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, Duration ttl) {
        return delegate.retrieve(name, key, ttl);
    }
}