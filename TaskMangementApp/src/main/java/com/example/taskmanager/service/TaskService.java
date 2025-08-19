package com.example.taskmanager.service;

import com.example.taskmanager.domain.ActivityType;
import com.example.taskmanager.domain.BoardList;
import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.repo.BoardListRepository;
import com.example.taskmanager.repo.TaskRepository;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.web.dto.task.TaskCreateRequest;
import com.example.taskmanager.web.dto.task.TaskResponse;
import com.example.taskmanager.web.dto.task.TaskUpdateRequest;
import com.example.taskmanager.security.Sanitize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository tasks;
    private final BoardListRepository lists;
    private final UserRepository users;
    private final ActivityService activity;
    private final EmailService email;

    public TaskService(TaskRepository tasks,
                       BoardListRepository lists,
                       UserRepository users,
                       ActivityService activity,
                       EmailService email) {
        this.tasks = tasks;
        this.lists = lists;
        this.users = users;
        this.activity = activity;
        this.email = email;
    }

    public List<TaskResponse> getTasks(Long listId) {
        return tasks.findByListId(listId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse createTask(Long listId, TaskCreateRequest req) {
        BoardList list = lists.findById(listId)
                .orElseThrow(() -> new IllegalArgumentException("List not found"));

        Task t = new Task();
        t.title = Sanitize.html(req.title);
        t.description = Sanitize.html(req.description);
        t.dueDate = req.dueDate;
        if (req.status != null) t.status = req.status;
        t.list = list;

        if (req.assigneeIds != null && !req.assigneeIds.isEmpty()) {
            var assigned = loadUsersOrThrow(req.assigneeIds);
            t.assignees = assigned;
        }

        Task saved = tasks.save(t);
        if (saved.assignees != null) {
            for (com.example.taskmanager.domain.User u : saved.assignees) {
                email.sendTaskAssignment(u, saved);
            }
        }
        // TODO: replace actor with current user id when available in service layer
        activity.log(list.board.id, list.board.ownerId, ActivityType.TASK_CREATED, "Task #" + saved.id + " created");
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest req) {
        Task t = tasks.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (req.title != null) t.title = Sanitize.html(req.title);
        if (req.description != null) t.description = Sanitize.html(req.description);
        if (req.dueDate != null) t.dueDate = req.dueDate;

        TaskStatus old = t.status;
        if (req.status != null) t.status = req.status;
        if (old != TaskStatus.DONE && t.status == TaskStatus.DONE && t.completedAt == null) {
            t.completedAt = java.time.Instant.now();
        }

        java.util.Set<com.example.taskmanager.domain.User> before =
            t.assignees == null ? new java.util.HashSet<>() : new java.util.HashSet<>(t.assignees);
        if (req.assigneeIds != null) {
            var newSet = loadUsersOrThrow(req.assigneeIds);
            t.assignees = newSet;
        }

        Task saved = tasks.save(t);
        if (req.assigneeIds != null && saved.assignees != null) {
            for (com.example.taskmanager.domain.User u : saved.assignees) {
                if (!before.contains(u)) {
                    email.sendTaskAssignment(u, saved);
                }
            }
        }
        activity.log(t.list.board.id, t.list.board.ownerId, ActivityType.TASK_UPDATED, "Task #" + t.id + " updated");
        return toResponse(saved);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task t = tasks.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        Long boardId = t.list.board.id;
        Long actorId = t.list.board.ownerId;
        tasks.deleteById(taskId);
        activity.log(boardId, actorId, ActivityType.TASK_DELETED, "Task #" + taskId + " deleted");
    }

    public Long getBoardIdForList(Long listId) {
        BoardList list = lists.findById(listId).orElseThrow(() -> new IllegalArgumentException("List not found"));
        return list.board.id;
    }

    @Transactional
    public Task moveTask(Long taskId, Long targetListId) {
        Task task = tasks.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        BoardList target = lists.findById(targetListId).orElseThrow(() -> new IllegalArgumentException("List not found"));
        Long boardId = task.list.board.id;
        if (!boardId.equals(target.board.id)) {
            throw new IllegalArgumentException("Cannot move across different boards");
        }
        task.list = target;
        Task saved = tasks.save(task);
        activity.log(boardId, target.board.ownerId, ActivityType.TASK_MOVED, "Task #" + task.id + " moved");
        return saved;
    }

    private TaskResponse toResponse(Task t) {
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
    private java.util.Set<com.example.taskmanager.domain.User> loadUsersOrThrow(java.util.List<Long> ids) {
        var found = users.findAllById(ids);
        java.util.Set<Long> unique = new java.util.HashSet<>(ids);
        if (found.size() != unique.size()) {
            throw new IllegalArgumentException("One or more userIds not found");
        }
        return new java.util.HashSet<>(found);
    }
}