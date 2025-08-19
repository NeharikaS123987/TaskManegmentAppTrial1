package com.example.taskmanager.web.dto.board;

import java.time.Instant;

public class BoardResponse {
    public Long id;
    public String name;
    public String description;
    public Long ownerId;
    public Instant createdAt;
    public Instant updatedAt;

    public BoardResponse(Long id, String name, String description, Long ownerId,
                         Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}