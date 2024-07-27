package com.blogging.dataprovider.cache;

import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomRedisCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter delegate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GenericJackson2JsonRedisSerializer serializer;

    public CustomRedisCacheWriter(RedisConnectionFactory connectionFactory,
                                  RedisTemplate<String, Object> redisTemplate,
                                  GenericJackson2JsonRedisSerializer serializer) {
        this.delegate = RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory);
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
    }

    @Override
    public void put(String name, byte[] key, byte[] value, Duration ttl) {
        String stringKey = new String(key);
        stringKey = stringKey.replace("{cacheName}", name);

        var ids = extractIds(stringKey);
        stringKey = removeLastMetadata(stringKey);
        stringKey = removeLastMetadata(stringKey);

        Object deserializedValue = serializer.deserialize(value);

        if (deserializedValue instanceof List l && l.size() == ids.size()) {
            for (int i = 0; i < l.size(); i++) {
                redisTemplate.opsForHash().put(stringKey, ids.get(i), l.get(i));
            }

            return;
        }

        if (deserializedValue instanceof Map m) {
            redisTemplate.opsForHash().putAll(stringKey, m);
            return;
        }

        redisTemplate.opsForHash().put(stringKey, ids.get(0), deserializedValue);
    }

    @Override
    public byte[] get(String name, byte[] key) {
        String stringKey = new String(key).replace("{cacheName}", name);
        List<String> ids = extractIds(stringKey);
        stringKey = removeLastMetadata(stringKey);
        String valueType = extractValueType(stringKey);
        stringKey = removeLastMetadata(stringKey);

        List<Object> redisResult = fetchFromRedis(stringKey, ids);

        if (redisResult == null) {
            return null;
        }

        if (valueType.equals("map")) {
            var result = ids.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            id -> redisResult.get(ids.indexOf(id))
                    ));
            return serializer.serialize(result);
        }

        if (valueType.equals("list")){
            return serializer.serialize(redisResult);
        }

        if (valueType.equals("obj")){
            return serializer.serialize(redisResult.get(0));
        }

        throw new IllegalArgumentException("Type not supported");
    }


    private List<Object> fetchFromRedis(String key, List<String> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No ids specified");
        }

        List<Object> redisResult = redisTemplate.opsForHash().multiGet(key, (List) ids);

        if (redisResult.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        return redisResult;
    }

    private static String removeLastMetadata(String input) {
        int lastColonIndex = input.lastIndexOf(":");
        return input.substring(0, lastColonIndex);
    }

    private String extractValueType(String stringKey) {
        if (stringKey.endsWith("list")) {
            return "list";
        }

        if (stringKey.endsWith("map")) {
            return "map";
        }

        if (stringKey.endsWith("obj")) {
            return "obj";
        }

        throw new IllegalArgumentException("Invalid value type");
    }

    private static List<String> extractIds(String input) {
        int lastColonIndex = input.lastIndexOf(":");

        if (lastColonIndex != -1 && lastColonIndex < input.length() - 1) {
            String idsPart = input.substring(lastColonIndex + 1);

            String[] ids = idsPart.split(",");

            return Arrays.stream(ids)
                    .map(String::trim)
                    .filter(trimmedId -> !trimmedId.isEmpty())
                    .toList();
        }

        return new ArrayList<>();
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