package com.example.taskmanager.controller;

import com.example.taskmanager.domain.Board;
import com.example.taskmanager.domain.BoardMember;
import com.example.taskmanager.domain.SystemRole;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.BoardMemberRepository;
import com.example.taskmanager.repo.BoardRepository;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.security.Sanitize;
import com.example.taskmanager.web.dto.board.BoardCreateRequest;
import com.example.taskmanager.web.dto.board.BoardResponse;
import com.example.taskmanager.web.dto.board.BoardUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardRepository boards;
    private final UserRepository users;
    private final BoardMemberRepository members;

    public BoardController(BoardRepository boards, UserRepository users, BoardMemberRepository members) {
        this.boards = boards;
        this.users = users;
        this.members = members;
    }

    private boolean isAdmin(User u) {
        return u != null && u.role == SystemRole.ADMIN;
    }

    // CREATE (owner from JWT)
    @PostMapping
    @CacheEvict(value = {"boardById", "boardsOfUser"}, allEntries = true)
    public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardCreateRequest req) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<BoardResponse>build();

        User owner = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        Board b = new Board();
        b.name = Sanitize.html(req.name);
        b.description = Sanitize.html(req.description);
        b.ownerId = owner.id;

        Board saved = boards.save(b);
        BoardResponse body = new BoardResponse(
                saved.id, saved.name, saved.description, saved.ownerId, saved.createdAt, saved.updatedAt
        );
        return ResponseEntity.created(URI.create("/api/boards/" + saved.id)).body(body);
    }

    // LIST:
    // - ADMIN: all boards
    // - USER: owned + member boards (dedup)
    @GetMapping
    @Cacheable(value = "boardsOfUser", key = "T(com.example.taskmanager.security.SecurityUtils).getCurrentUsername()")
    public ResponseEntity<List<BoardResponse>> list() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<java.util.List<BoardResponse>>build();

        User current = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        if (isAdmin(current)) {
            List<BoardResponse> all = boards.findAll().stream()
                    .map(b -> new BoardResponse(b.id, b.name, b.description, b.ownerId, b.createdAt, b.updatedAt))
                    .toList();
            return ResponseEntity.ok(all);
        }

        // Owned boards
        List<Board> owned = boards.findByOwnerId(current.id);

        // Boards where current user is a member
        List<BoardMember> links = members.findByUserId(current.id);
        Set<Long> memberBoardIds = links.stream().map(m -> m.board.id).collect(Collectors.toSet());
        List<Board> memberBoards = memberBoardIds.isEmpty()
                ? List.of()
                : boards.findAllById(memberBoardIds);

        // Combine & deduplicate by id
        Map<Long, Board> combined = new LinkedHashMap<>();
        owned.forEach(b -> combined.put(b.id, b));
        memberBoards.forEach(b -> combined.putIfAbsent(b.id, b));

        List<BoardResponse> result = combined.values().stream()
                .map(b -> new BoardResponse(b.id, b.name, b.description, b.ownerId, b.createdAt, b.updatedAt))
                .toList();

        return ResponseEntity.ok(result);
    }

    // GET BY ID (owner OR member OR ADMIN)
    @GetMapping("/{id}")
    @Cacheable(value = "boardById", key = "#id")
    public ResponseEntity<BoardResponse> get(@PathVariable Long id) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<BoardResponse>build();

        User current = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        return boards.findById(id)
                .map(b -> {
                    boolean allowed = isAdmin(current)
                            || b.ownerId.equals(current.id)
                            || members.existsByBoardIdAndUserId(b.id, current.id);
                    if (!allowed) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<BoardResponse>build();
                    }
                    return ResponseEntity.ok(new BoardResponse(
                            b.id, b.name, b.description, b.ownerId, b.createdAt, b.updatedAt));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<BoardResponse>build());
    }

    // UPDATE (owner OR ADMIN)
    @PutMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(value = "boardById", key = "#id"),
        @CacheEvict(value = "boardsOfUser", allEntries = true)
    })
    public ResponseEntity<BoardResponse> update(@PathVariable Long id, @Valid @RequestBody BoardUpdateRequest req) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<BoardResponse>build();

        User current = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        return boards.findById(id).map(b -> {
            if (!isAdmin(current) && !b.ownerId.equals(current.id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).<BoardResponse>build();
            }
            if (req.name != null) b.name = Sanitize.html(req.name);
            if (req.description != null) b.description = Sanitize.html(req.description);
            Board saved = boards.save(b);
            return ResponseEntity.ok(new BoardResponse(
                    saved.id, saved.name, saved.description, saved.ownerId, saved.createdAt, saved.updatedAt
            ));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<BoardResponse>build());
    }

    // DELETE (owner OR ADMIN)
    @DeleteMapping("/{id}")
    @Caching(evict = {
        @CacheEvict(value = "boardById", key = "#id"),
        @CacheEvict(value = "boardsOfUser", allEntries = true)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<Void>build();

        User current = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        Board b = boards.findById(id).orElse(null);
        if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build();
        if (!isAdmin(current) && !b.ownerId.equals(current.id)) {
            if (b == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build();
        }

        boards.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}