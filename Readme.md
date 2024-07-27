# Custom Redis Cache Writer Demo

This Spring Boot application demonstrates how to customize Redis cache writing for improved Redis data transparency. It showcases a custom implementation of `RedisCacheWriter` that allows for more granular control over how data is stored and retrieved from Redis.

## Features

- Custom `RedisCacheWriter` implementation
- Support for caching different data structures (single objects, lists, and maps)
- Efficient storage and retrieval of cached data
- Custom key generation for cache entries
- Integration with Spring Boot and Spring Cache

## Project Structure

- `CacheDemoApplication.java`: Main Spring Boot application class
- `DataProviderController.java`: REST controller with cacheable endpoints
- `User.java`: Simple POJO representing a user
- `CacheConfig.java`: Configuration for Redis cache and custom components
- `CustomCacheKeyGenerator.java`: Custom key generator for cache entries
- `CustomRedisCacheWriter.java`: Custom implementation of `RedisCacheWriter`
- `CustomRedisCacheWriterHelper.java`: Helper class for cache writing operations

## Key Components

### CustomRedisCacheWriter

This class overrides the default `RedisCacheWriter` to provide custom logic for storing and retrieving data from Redis. It supports different data structures and optimizes storage based on the type of data being cached.

### CustomCacheKeyGenerator

Generates cache keys based on method signatures and parameters, allowing for more flexible and descriptive cache keys.

### CacheConfig

Configures the Redis cache manager, custom key generator, and other necessary beans for the caching system.

## How It Works

1. The `@Cacheable` annotation is used on controller methods to enable caching.
2. The custom key generator creates unique keys for each cache entry.
3. When data is cached, the `CustomRedisCacheWriter` determines the data structure and stores it efficiently in Redis.
4. On cache hits, the writer retrieves and deserializes the data appropriately based on its structure.

## Getting Started

1. Ensure you have Redis installed and running on your local machine.
2. Clone this repository.
3. Run the Spring Boot application: