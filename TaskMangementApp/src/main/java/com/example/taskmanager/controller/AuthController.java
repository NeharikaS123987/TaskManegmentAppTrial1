package com.example.taskmanager.controller;

import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.web.dto.JwtResponse;
import com.example.taskmanager.web.dto.LoginRequest;
import com.example.taskmanager.web.dto.SignupRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> signup(@Valid @RequestBody SignupRequest req) {
        String token = auth.signup(req.name, req.email, req.password);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = auth.login(req.email, req.password);
        return ResponseEntity.ok(new JwtResponse(token));
    }
}