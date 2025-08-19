package com.example.taskmanager.controller;

import com.example.taskmanager.domain.BoardActivity;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.ActivityService;
import com.example.taskmanager.service.BoardAccessService;
import com.example.taskmanager.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/{boardId}/activity")
public class BoardActivityController {

    private final ActivityService activity;
    private final BoardAccessService access;
    private final UserRepository users;

    public BoardActivityController(ActivityService activity, BoardAccessService access, UserRepository users) {
        this.activity = activity;
        this.access = access;
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<Page<BoardActivity>> list(@PathVariable Long boardId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User current = users.findByEmail(email).orElse(null);
        if (!access.canViewBoard(current, boardId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Page<BoardActivity> result = activity.getBoardActivity(boardId, PageRequest.of(Math.max(page, 0), Math.min(size, 100)));
        return ResponseEntity.ok(result);
    }
}