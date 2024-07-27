package com.blogging.dataprovider.cache.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class CustomRedisCacheWriterHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    String processKey(String name, byte[] key) {
        String stringKey = new String(key).replace("{cacheName}", name);
        return removeLastMetadata(removeLastMetadata(stringKey));
    }

    void putList(String key, List<?> list) {
        List<String> ids = extractIds(key);
        IntStream.range(0, list.size())
                .forEach(i -> redisTemplate.opsForHash().put(key, ids.get(i), list.get(i)));
    }

    void putMap(String key, Map<?, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    void putSingle(String key, Object value) {
        List<String> ids = extractIds(key);
        redisTemplate.opsForHash().put(key, ids.get(0), value);
    }

    Map<String, Object> createResultMap(List<String> ids, List<Object> redisResult) {
        return ids.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> redisResult.get(ids.indexOf(id))
                ));
    }


    List<Object> fetchFromRedis(String key, List<String> ids) {
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

    static String extractValueType(String stringKey) {
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

    static List<String> extractIds(String input) {
        int lastColonIndex = input.lastIndexOf(":");

        if (isColonFound(lastColonIndex, input)) {
            String idsPart = input.substring(lastColonIndex + 1);

            String[] ids = idsPart.split(",");

            return Arrays.stream(ids)
                    .map(String::trim)
                    .filter(trimmedId -> !trimmedId.isEmpty())
                    .toList();
        }

        return new ArrayList<>();
    }

    private static boolean isColonFound(int lastColonIndex, String input) {
        return lastColonIndex != -1 && lastColonIndex < input.length() - 1;
    }

}
