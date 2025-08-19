package com.example.taskmanager.web.dto.task;

import com.example.taskmanager.domain.TaskStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class TaskUpdateRequest {
    @Size(min = 1, max = 200)
    public String title;

    @Size(max = 4000)
    public String description;

    public LocalDate dueDate;
    public TaskStatus status;

    // if null → leave unchanged; if provided → replace entire assignee set
    public List<Long> assigneeIds;
}