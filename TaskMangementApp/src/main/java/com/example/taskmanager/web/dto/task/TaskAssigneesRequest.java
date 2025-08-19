package com.example.taskmanager.web.dto.task;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class TaskAssigneesRequest {
    @NotEmpty
    public List<Long> userIds;
}