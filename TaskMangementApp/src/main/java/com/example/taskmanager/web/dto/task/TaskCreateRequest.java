package com.example.taskmanager.web.dto.task;

import com.example.taskmanager.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class TaskCreateRequest {
    @NotBlank
    @Size(min = 1, max = 200)
    public String title;

    @Size(max = 4000)
    public String description;

    public LocalDate dueDate;
    public TaskStatus status; // optional; defaults to TODO if null

    // optional: if provided, will assign these users on create
    public List<Long> assigneeIds;
}