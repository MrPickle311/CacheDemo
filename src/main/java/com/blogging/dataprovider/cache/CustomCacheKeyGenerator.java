package com.blogging.dataprovider.cache;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder keyBuilder = new StringBuilder();

        createKeyPrefix(target, keyBuilder, method);

        if(params.length == 1 && params[0] instanceof Collection ids){
            return getKeyForListOfParams(ids, keyBuilder);
        }

        if(params.length == 1 && params[0] instanceof String id){
            return getKeyForSingleParam(id, keyBuilder);
        }

        throw new IllegalArgumentException("Cache key is required");
    }

    private static void createKeyPrefix(final Object target,
                                        StringBuilder keyBuilder,
                                        Method method) {
        keyBuilder
                .append(target.getClass().getName())
                .append(":")
                .append("{cacheName}")
                .append(":")
                .append(getTypeMetadataFromReturnType(method))
                .append(":");
    }

    private static String getKeyForSingleParam(String id, StringBuilder keyBuilder) {
        return keyBuilder.append(id).append(",").toString();
    }

    private static String getKeyForListOfParams(final Collection<String> ids, StringBuilder keyBuilder) {
        ids.forEach(id -> keyBuilder.append(id).append(","));
        return keyBuilder.toString();
    }

    private static String getTypeMetadataFromReturnType(Method method) {
        if (method.getGenericReturnType() instanceof ParameterizedType returnType) {
            Class<?> rawType = (Class<?>) returnType.getRawType();

            if (List.class.isAssignableFrom(rawType)) {
                return "list";
            }

            if (Map.class.isAssignableFrom(rawType)) {
                return "map";
            }
        }

        return "obj";
    }
}
