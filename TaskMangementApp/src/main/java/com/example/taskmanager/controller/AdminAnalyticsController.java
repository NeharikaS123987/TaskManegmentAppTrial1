package com.example.taskmanager.controller;

import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analytics;
    private final UserRepository users;

    public AdminAnalyticsController(AnalyticsService analytics, UserRepository users) {
        this.analytics = analytics; this.users = users;
    }

    @GetMapping("/boards/{boardId}")
    public ResponseEntity<Map<String, Object>> boardSummary(@PathVariable Long boardId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User current = users.findByEmail(email).orElse(null);
        try {
            return ResponseEntity.ok(analytics.boardSummary(current, boardId));
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}