package com.example.taskmanager.service;

import com.example.taskmanager.domain.*;
import com.example.taskmanager.repo.BoardActivityRepository;
import com.example.taskmanager.repo.BoardRepository;
import com.example.taskmanager.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final BoardActivityRepository activityRepo;
    private final BoardRepository boards;
    private final UserRepository users;
    private final BoardEventService events;

    public ActivityService(BoardActivityRepository activityRepo,
                           BoardRepository boards,
                           UserRepository users,
                           BoardEventService events) {
        this.activityRepo = activityRepo;
        this.boards = boards;
        this.users = users;
        this.events = events;
    }

    public void log(Long boardId, Long actorId, ActivityType type, String detail) {
        Board board = boards.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Board not found"));
        User actor = users.findById(actorId).orElseThrow(() -> new IllegalArgumentException("Actor not found"));
        BoardActivity a = new BoardActivity();
        a.board = board;
        a.actor = actor;
        a.type = type;
        a.detail = detail;
        activityRepo.save(a);
        // also push to SSE subscribers
        events.broadcast(boardId, type, detail);
    }

    public Page<BoardActivity> getBoardActivity(Long boardId, Pageable pageable) {
        return activityRepo.findByBoardIdOrderByIdDesc(boardId, pageable);
    }
}