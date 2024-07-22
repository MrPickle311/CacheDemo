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

    // 1. Simple key-value caching
    @Cacheable(value = "userCache", key = "#userId")
    @GetMapping("/users/{userId}")
    @SneakyThrows
    public User getUser(@PathVariable String userId) {
        // Simulate database fetch
        Thread.sleep(2000);
        return new User(userId, "User " + userId, "user" + userId + "@example.com");
    }

    // 2. Hash-based caching
    @Cacheable(value = "productCache",
            key = "")
    @GetMapping("/products")
    @SneakyThrows
    public Map<String, Product> getProduct(@RequestParam("productIds") List<String> productIds) {
        // Simulate database fetch
        Thread.sleep(2000);
        return productIds.stream().map(id ->
                        Map.entry(id, new Product(id, "Product " + id, 99.99, "Electronics")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // 3. List caching
    @Cacheable(value = "recentActivities", key = "#userId")
    @GetMapping("/users/{userId}/activities")
    @SneakyThrows
    public List<Activity> getUserActivities(@PathVariable String userId) {
        // Simulate database fetch
        Thread.sleep(2000);
        return Arrays.asList(
                new Activity(userId, "Logged in", LocalDateTime.now().minusHours(2)),
                new Activity(userId, "Updated profile", LocalDateTime.now().minusHours(1)),
                new Activity(userId, "Made a purchase", LocalDateTime.now())
        );
    }

    // 4. Set caching
    @Cacheable(value = "uniqueVisitors", key = "#date")
    @GetMapping("/visitors/{date}")
    @SneakyThrows
    public Set<Visitor> getUniqueVisitors(@PathVariable String date) {
        LocalDate visitDate = LocalDate.parse(date);
        // Simulate database fetch
        Thread.sleep(2000);
        return new HashSet<>(Arrays.asList(
                new Visitor("user1", visitDate),
                new Visitor("user2", visitDate),
                new Visitor("user3", visitDate)
        ));
    }

    // 5. Sorted set caching
    @Cacheable(value = "leaderboard")
    @GetMapping("/leaderboard")
    @SneakyThrows
    public List<LeaderboardEntry> getLeaderboard() {
        // Simulate database fetch
        Thread.sleep(2000);
        return Arrays.asList(
                new LeaderboardEntry("user1", 100),
                new LeaderboardEntry("user2", 85),
                new LeaderboardEntry("user3", 95)
        );
    }

}
