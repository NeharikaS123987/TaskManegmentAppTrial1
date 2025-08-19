package com.example.taskmanager.web.dto.board;

import jakarta.validation.constraints.NotBlank;

public class BoardListDto {
    public Long id;

    @NotBlank
    public String name;
}