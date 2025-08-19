package com.example.taskmanager.controller;

import com.example.taskmanager.domain.User;
import com.example.taskmanager.security.SecurityUtils;
import com.example.taskmanager.service.BoardAccessService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.web.dto.task.TaskCreateRequest;
import com.example.taskmanager.web.dto.task.TaskResponse;
import com.example.taskmanager.web.dto.task.TaskUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists/{listId}/tasks")
public class TaskController {

    private final TaskService service;
    private final BoardAccessService access;
    private final UserRepository users;

    public TaskController(TaskService service, BoardAccessService access, UserRepository users) {
        this.service = service;
        this.access = access;
        this.users = users;
    }

    @GetMapping
    public List<TaskResponse> getTasks(@PathVariable Long listId) {
        return service.getTasks(listId);
    }

    @PostMapping
    public TaskResponse createTask(@PathVariable Long listId,
                                   @Valid @RequestBody TaskCreateRequest req) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Long boardId = service.getBoardIdForList(listId);
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);

        return service.createTask(listId, req);
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(@PathVariable Long listId,
                                   @PathVariable Long taskId,
                                   @Valid @RequestBody TaskUpdateRequest req) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Long boardId = service.getBoardIdForList(listId);
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);

        return service.updateTask(taskId, req);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long listId, @PathVariable Long taskId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Long boardId = service.getBoardIdForList(listId);
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);

        service.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/move/{targetListId}")
    public TaskResponse moveTask(@PathVariable Long listId,
                                 @PathVariable Long taskId,
                                 @PathVariable Long targetListId) {
        String email = SecurityUtils.getCurrentUsername();
        if (email == null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Long boardId = service.getBoardIdForList(listId);
        User current = users.findByEmail(email).orElse(null);
        if (!access.canEditContent(current, boardId)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);

        var t = service.moveTask(taskId, targetListId);
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
    }
}