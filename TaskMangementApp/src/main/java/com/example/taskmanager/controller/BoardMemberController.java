package com.example.taskmanager.controller;

import com.example.taskmanager.domain.Board;
import com.example.taskmanager.domain.BoardMember;
import com.example.taskmanager.domain.SystemRole;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.BoardMemberRepository;
import com.example.taskmanager.repo.BoardRepository;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.web.dto.board.AddMemberRequest;
import com.example.taskmanager.web.dto.board.BoardMemberResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/members")
public class BoardMemberController {

    private final BoardRepository boards;
    private final UserRepository users;
    private final BoardMemberRepository members;

    public BoardMemberController(BoardRepository boards, UserRepository users, BoardMemberRepository members) {
        this.boards = boards;
        this.users = users;
        this.members = members;
    }

    private boolean isAdmin(User u) { return u != null && u.role == SystemRole.ADMIN; }

    // GET members — owner, any member, or ADMIN
    @GetMapping
    public ResponseEntity<List<BoardMemberResponse>> list(@PathVariable Long boardId) {
        var current = getCurrentUserOr401();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<List<BoardMemberResponse>>build();

        Board board = boards.findById(boardId).orElse(null);
        if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).<List<BoardMemberResponse>>build();

        if (!isAdmin(current) && !isOwnerOrMember(board, current)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<List<BoardMemberResponse>>build();
        }

        List<BoardMemberResponse> body = members.findByBoardId(boardId).stream()
                .map(m -> new BoardMemberResponse(m.user.id, m.user.name, m.user.email, m.role.name()))
                .toList();

        return ResponseEntity.ok(body);
    }

    // POST add member — owner OR ADMIN
    @PostMapping
    public ResponseEntity<BoardMemberResponse> add(@PathVariable Long boardId, @Valid @RequestBody AddMemberRequest req) {
        var current = getCurrentUserOr401();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<BoardMemberResponse>build();

        Board board = boards.findById(boardId).orElse(null);
        if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).<BoardMemberResponse>build();

        if (!isAdmin(current) && !isOwner(board, current)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).<BoardMemberResponse>build();
        }

        User target = null;
        if (req.userId != null) {
            target = users.findById(req.userId).orElse(null);
        } else if (req.email != null) {
            target = users.findByEmail(req.email.toLowerCase()).orElse(null);
        }
        if (target == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).<BoardMemberResponse>build();

        if (target.id.equals(board.ownerId)) {
            // already the owner; no-op
            return ResponseEntity.status(HttpStatus.CONFLICT).<BoardMemberResponse>build();
        }
        if (members.existsByBoardIdAndUserId(boardId, target.id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).<BoardMemberResponse>build();
        }

        BoardMember m = new BoardMember();
        m.board = board;
        m.user = target;
        m.role = (req.role == null) ? com.example.taskmanager.domain.BoardRole.VIEWER : req.role;
        BoardMember saved = members.save(m);

        var body = new BoardMemberResponse(saved.user.id, saved.user.name, saved.user.email, saved.role.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // DELETE remove member — owner OR ADMIN
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@PathVariable Long boardId, @PathVariable Long userId) {
        var current = getCurrentUserOr401();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Board board = boards.findById(boardId).orElse(null);
        if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!isAdmin(current) && !isOwner(board, current)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!members.existsByBoardIdAndUserId(boardId, userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        members.deleteByBoardIdAndUserId(boardId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<BoardMemberResponse> changeRole(@PathVariable Long boardId,
                                                          @PathVariable Long userId,
                                                          @RequestBody @Valid com.example.taskmanager.web.dto.board.UpdateMemberRoleRequest body) {
        var current = getCurrentUserOr401();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Board board = boards.findById(boardId).orElse(null);
        if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!isAdmin(current) && !isOwner(board, current)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var link = members.findByBoardId(boardId).stream()
                .filter(m -> m.user.id.equals(userId))
                .findFirst().orElse(null);
        if (link == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        link.role = body.role;
        var saved = members.save(link);
        return ResponseEntity.ok(new BoardMemberResponse(saved.user.id, saved.user.name, saved.user.email, saved.role.name()));
    }

    // --- helpers ---
    private User getCurrentUserOr401() {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return null;
        return users.findByEmail(email).orElse(null);
    }

    private boolean isOwner(Board board, User user) {
        return board.ownerId != null && board.ownerId.equals(user.id);
    }

    private boolean isOwnerOrMember(Board board, User user) {
        if (isOwner(board, user)) return true;
        return members.existsByBoardIdAndUserId(board.id, user.id);
    }
}