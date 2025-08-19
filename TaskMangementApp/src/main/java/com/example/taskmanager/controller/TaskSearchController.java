package com.example.taskmanager.controller;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.BoardAccessService;
import com.example.taskmanager.repo.TaskRepository;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.web.dto.task.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/boards/{boardId}/tasks")
public class TaskSearchController {

    private final TaskRepository tasks;
    private final BoardAccessService access;
    private final UserRepository users;

    public TaskSearchController(TaskRepository tasks, BoardAccessService access, UserRepository users) {
        this.tasks = tasks;
        this.access = access;
        this.users = users;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponse>> search(
            @PathVariable Long boardId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        User current = users.findByEmail(email).orElse(null);
        if (!access.canViewBoard(current, boardId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Page<Task> result = tasks.searchInBoard(
                boardId, q, status, assigneeId, dueAfter, dueBefore,
                PageRequest.of(Math.max(0, page), Math.min(100, size))
        );

        Page<TaskResponse> mapped = result.map(t -> {
            java.util.List<Long> assigneeIds = (t.assignees == null)
                    ? java.util.List.of()
                    : t.assignees.stream().map(u -> u.id).toList();
            return new TaskResponse(
                    t.id,
                    t.title,
                    t.description,
                    t.dueDate,
                    t.status,
                    t.list.id,
                    assigneeIds,
                    t.createdAt,
                    t.updatedAt
            );
        });

        return ResponseEntity.ok(mapped);
    }
}