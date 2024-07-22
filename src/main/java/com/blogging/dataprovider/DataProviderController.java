package com.blogging.dataprovider;

import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog-cache")
public class DataProviderController {

    @Cacheable(value = "userCache", key = "#userId")
    @GetMapping("/users/{userId}")
    @SneakyThrows
    public User getUser(@PathVariable String userId) {
        simulateDatabaseFetch();
        return new User(userId, "User " + userId, "user" + userId + "@example.com");
    }

    @Cacheable(value = "productCache",
            keyGenerator = "customKeyGenerator")
    @GetMapping("/products")
    @SneakyThrows
    public Map<String, Product> getProduct(@RequestParam("productIds") List<String> productIds) {
        simulateDatabaseFetch();
        return productIds.stream().map(id ->
                        Map.entry(id, new Product(id, "Product " + id, 99.99, "Electronics")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static void simulateDatabaseFetch() throws InterruptedException {
        Thread.sleep(2000);
    }

    @Cacheable(value = "recentActivities", key = "#userId")
    @GetMapping("/users/{userId}/activities")
    @SneakyThrows
    public List<Activity> getUserActivities(@PathVariable String userId) {
        simulateDatabaseFetch();
        return Arrays.asList(
                new Activity(userId, "Logged in", LocalDateTime.now().minusHours(2)),
                new Activity(userId, "Updated profile", LocalDateTime.now().minusHours(1)),
                new Activity(userId, "Made a purchase", LocalDateTime.now())
        );
    }
}
