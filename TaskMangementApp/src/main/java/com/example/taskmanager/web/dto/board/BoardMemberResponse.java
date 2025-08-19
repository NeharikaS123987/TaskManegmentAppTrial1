package com.example.taskmanager.web.dto.board;

public class BoardMemberResponse {
    public Long userId;
    public String name;
    public String email;
    public String role;

    public BoardMemberResponse(Long userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}