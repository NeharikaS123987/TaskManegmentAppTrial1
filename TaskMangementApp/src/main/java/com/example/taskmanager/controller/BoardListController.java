package com.example.taskmanager.controller;

import com.example.taskmanager.domain.BoardList;
import com.example.taskmanager.service.BoardListService;
import com.example.taskmanager.web.dto.board.BoardListDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.BoardAccessService;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/lists")
public class BoardListController {

    private final BoardListService service;
    private final BoardAccessService access;
    private final UserRepository users;

    public BoardListController(BoardListService service, BoardAccessService access, UserRepository users) {
        this.service = service;
        this.access = access;
        this.users = users;
    }

    @GetMapping
    public List<BoardList> getLists(@PathVariable Long boardId) {
        return service.getLists(boardId);
    }

    @PostMapping
    public BoardList createList(@PathVariable Long boardId,
                                @Valid @RequestBody BoardListDto dto) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED); }
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN); }
        return service.createList(boardId, dto.name);
    }

    @PutMapping("/{listId}")
    public BoardList updateList(@PathVariable Long boardId,
                                @PathVariable Long listId,
                                @Valid @RequestBody BoardListDto dto) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED); }
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN); }
        // boardId not used yet, but kept for URL consistency
        return service.updateList(listId, dto.name);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<?> deleteList(@PathVariable Long boardId,
                                        @PathVariable Long listId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED); }
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) { throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN); }
        service.deleteList(listId);
        return ResponseEntity.noContent().build();
    }
}