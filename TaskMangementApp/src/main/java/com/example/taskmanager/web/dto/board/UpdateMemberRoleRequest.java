package com.example.taskmanager.web.dto.board;

import com.example.taskmanager.domain.BoardRole;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {
    @NotNull
    public BoardRole role;
}
