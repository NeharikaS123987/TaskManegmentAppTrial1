package com.example.taskmanager.service;

import com.example.taskmanager.domain.Board;
import com.example.taskmanager.domain.BoardRole;
import com.example.taskmanager.domain.SystemRole;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.BoardMemberRepository;
import com.example.taskmanager.repo.BoardRepository;
import org.springframework.stereotype.Service;

@Service
public class BoardAccessService {

    private final BoardRepository boards;
    private final BoardMemberRepository members;

    public BoardAccessService(BoardRepository boards, BoardMemberRepository members) {
        this.boards = boards;
        this.members = members;
    }

    public boolean canManageBoard(User user, Long boardId) {
        if (user == null) return false;
        if (user.role == SystemRole.ADMIN) return true;
        Board b = boards.findById(boardId).orElse(null);
        return b != null && b.ownerId != null && b.ownerId.equals(user.id);
    }

    public boolean canViewBoard(User user, Long boardId) {
        if (user == null) return false;
        if (user.role == SystemRole.ADMIN) return true;
        Board b = boards.findById(boardId).orElse(null);
        if (b == null) return false;
        if (b.ownerId.equals(user.id)) return true;
        return members.existsByBoardIdAndUserId(boardId, user.id);
    }

    public boolean canEditContent(User user, Long boardId) {
        if (user == null) return false;
        if (canManageBoard(user, boardId)) return true;
        return members.findByBoardId(boardId).stream()
                .anyMatch(m -> m.user.id.equals(user.id) && m.role == BoardRole.EDITOR);
    }
}