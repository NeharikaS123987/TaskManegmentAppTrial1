package com.example.taskmanager.web.dto.task;

import com.example.taskmanager.domain.TaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class TaskResponse {
    public Long id;
    public String title;
    public String description;
    public LocalDate dueDate;
    public TaskStatus status;
    public Long listId;
    public List<Long> assigneeIds;
    public Instant createdAt;
    public Instant updatedAt;

    public TaskResponse(Long id,
                        String title,
                        String description,
                        LocalDate dueDate,
                        TaskStatus status,
                        Long listId,
                        List<Long> assigneeIds,
                        Instant createdAt,
                        Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.listId = listId;
        this.assigneeIds = assigneeIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}