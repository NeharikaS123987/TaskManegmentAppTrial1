package com.example.taskmanager.web.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardCreateRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    public String name;

    @Size(max = 1000)
    public String description;
}