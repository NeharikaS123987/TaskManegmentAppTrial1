package com.example.taskmanager.web.dto.board;

import com.example.taskmanager.domain.BoardRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;

public class AddMemberRequest {
    @Positive
    public Long userId;

    @Email
    public String email;

    // optional; defaults to VIEWER
    public BoardRole role;
}