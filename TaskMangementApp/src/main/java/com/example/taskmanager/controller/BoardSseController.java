package com.example.taskmanager.controller;

import com.example.taskmanager.domain.User;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.BoardAccessService;
import com.example.taskmanager.service.BoardEventService;
import com.example.taskmanager.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/boards/{boardId}/events")
public class BoardSseController {

    private final BoardEventService events;
    private final BoardAccessService access;
    private final UserRepository users;

    public BoardSseController(BoardEventService events, BoardAccessService access, UserRepository users) {
        this.events = events;
        this.access = access;
        this.users = users;
    }

    @GetMapping("/sse")
    public ResponseEntity<SseEmitter> stream(@PathVariable Long boardId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User current = users.findByEmail(email).orElse(null);
        if (!access.canViewBoard(current, boardId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        SseEmitter emitter = events.subscribe(boardId);
        return ResponseEntity.ok(emitter);
    }
}