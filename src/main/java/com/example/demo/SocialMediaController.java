package com.example.demo;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SocialMediaController {

    private final String BASE_URL = "http://20.244.56.144/evaluation-service";
    private final String BEARER_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJNYXBDbGFpbXMiOnsiZXhwIjoxNzQ0Mzg1MzQyLCJpYXQiOjE3NDQzODUwNDIsImlzcyI6IkFmZm9yZG1lZCIsImp0aSI6ImE2OTA5NTRiLTkxNDQtNDdmOS05ZDM0LWVmNDUzODVkNzM0ZiIsInN1YiI6InZpZ2huZXNoc2hlcmVnYXIyMDA0QGdtYWlsLmNvbSJ9LCJlbWFpbCI6InZpZ2huZXNoc2hlcmVnYXIyMDA0QGdtYWlsLmNvbSIsIm5hbWUiOiJiIHZpZ2huZXNoIGt1bWFyIiwicm9sbE5vIjoiNGNiMjJjczAyNCIsImFjY2Vzc0NvZGUiOiJuWllEcUgiLCJjbGllbnRJRCI6ImE2OTA5NTRiLTkxNDQtNDdmOS05ZDM0LWVmNDUzODVkNzM0ZiIsImNsaWVudFNlY3JldCI6ImhCYmNnTnR0SnlGc21uRmgifQ.qI5ErZ4qL95eE38v-4fZx1i8d1Ea_XraNK3tK5qKB8E"
            + "7zj3ML2vXrVCvcZEcL3G8SXcOMn4gaqAe5AhWpZBJr4"; // replace full token

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpEntity<String> getAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", BEARER_TOKEN);
        return new HttpEntity<>(headers);
    }

    @Cacheable("users")
    public Map<String, String> getUsers() {
        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/users", HttpMethod.GET, getAuthEntity(), Map.class);
        return (Map<String, String>) response.getBody().get("users");
    }

    @Cacheable("posts")
    public List<Map<String, Object>> getUserPosts(String userId) {
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/users/" + userId + "/posts", HttpMethod.GET, getAuthEntity(), List.class);
        return response.getBody();
    }

    @Cacheable("comments")
    public List<Map<String, Object>> getPostComments(String postId) {
        ResponseEntity<List> response = restTemplate.exchange(
                BASE_URL + "/posts/" + postId + "/comments", HttpMethod.GET, getAuthEntity(), List.class);
        return response.getBody();
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers() {
        Map<String, String> users = getUsers();
        List<Map<String, Object>> summary = new ArrayList<>();

        for (String userId : users.keySet()) {
            List<Map<String, Object>> posts = getUserPosts(userId);
            int totalComments = posts.stream()
                    .map(post -> getPostComments(String.valueOf(post.get("id"))))
                    .mapToInt(List::size)
                    .sum();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userId);
            userMap.put("name", users.get(userId));
            userMap.put("total_comments", totalComments);
            summary.add(userMap);
        }

        List<Map<String, Object>> topFive = summary.stream()
                .sorted((a, b) -> (int) b.get("total_comments") - (int) a.get("total_comments"))
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(topFive);
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPostsByType(@RequestParam String type) {
        if (!type.equals("popular") && !type.equals("latest")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid type. Use 'popular' or 'latest'."));
        }

        Map<String, String> users = getUsers();
        List<Map<String, Object>> allPosts = new ArrayList<>();
        for (String userId : users.keySet()) {
            allPosts.addAll(getUserPosts(userId));
        }

        if (type.equals("popular")) {
            int maxComments = 0;
            List<Map<String, Object>> popular = new ArrayList<>();

            for (Map<String, Object> post : allPosts) {
                String postId = String.valueOf(post.get("id"));
                int count = getPostComments(postId).size();

                if (count > maxComments) {
                    maxComments = count;
                    popular = new ArrayList<>();
                    popular.add(post);
                } else if (count == maxComments) {
                    popular.add(post);
                }
            }

            return ResponseEntity.ok(Map.of("popular_posts", popular));
        }

        if (type.equals("latest")) {
            List<Map<String, Object>> latest = allPosts.stream()
                    .sorted((a, b) -> String.valueOf(b.getOrDefault("timestamp", ""))
                            .compareTo(String.valueOf(a.getOrDefault("timestamp", ""))))
                    .limit(5)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("latest_posts", latest));
        }

        return ResponseEntity.ok().build();
    }
}
