package com.example.taskmanager.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {
    @NotBlank @Size(min = 2, max = 100)
    public String name;

    @NotBlank @Email
    public String email;

    @NotBlank @Size(min = 6, max = 100)
    public String password;
}