package com.blogging.dataprovider;

import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog-cache")
public class DataProviderController {

    @Cacheable(value = "userCache",
            keyGenerator = "customKeyGenerator")
    @GetMapping("/users/{userId}")
    @SneakyThrows
    public User getUser(@PathVariable String userId) {
        simulateDatabaseFetch();
        return new User(userId, "User " + userId, "user" + userId + "@example.com");
    }

    @Cacheable(value = "userCache",
            keyGenerator = "customKeyGenerator")
    @GetMapping("/userCache/map")
    @SneakyThrows
    public Map<String, User> getProduct(@RequestParam("userIds") List<String> userIds) {
        simulateDatabaseFetch();
        return userIds.stream().map(id ->
                        Map.entry(id, new User(id, "A name" + id, "abc@gm.com")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Cacheable(value = "userCache",
            keyGenerator = "customKeyGenerator")
    @GetMapping("/users/activities/list")
    @SneakyThrows
    public List<User> getUserActivities(@RequestParam("userIds") List<String> userIds) {
        simulateDatabaseFetch();
        return userIds.stream()
                .map(id -> new User(id, "A name" + id, "abc@gm.com"))
                .toList();
    }

    private static void simulateDatabaseFetch() throws InterruptedException {
        Thread.sleep(2000);
    }
}
