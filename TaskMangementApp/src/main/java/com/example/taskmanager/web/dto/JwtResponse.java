package com.example.taskmanager.web.dto;

public class JwtResponse {
    public String accessToken;

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}