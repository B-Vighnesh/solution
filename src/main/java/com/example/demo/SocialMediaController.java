package com.example.demo;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/social")
public class SocialMediaController {

    RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getTopUsers() {
        String url = "http://20.244.56.144/evaluation-service/users/top-users";
        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                List.class
        );

        // Automatically returns list of LinkedHashMap, so no casting issue
        List<Map<String, Object>> users = response.getBody();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{userId}/posts")
    public ResponseEntity<List<Map<String, Object>>> getPosts(@PathVariable("userId") Long userId) {
        String url = "http://20.244.56.144/evaluation-service/users/" + userId + "/posts";
        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                List.class
        );

        List<Map<String, Object>> posts = response.getBody();
        return ResponseEntity.ok(posts);
    }
}
