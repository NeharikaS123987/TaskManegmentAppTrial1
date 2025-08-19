package com.example.taskmanager.service;

import com.example.taskmanager.domain.SystemRole;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.UserRepository;
import com.example.taskmanager.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwt) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    @Transactional
    public String signup(String name, String email, String rawPassword) {
        // Check if email is taken
        users.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already in use");
        });

        // Create user with hashed password
        User u = new User();
        u.name = name;
        u.email = email.toLowerCase();
        u.password = passwordEncoder.encode(rawPassword);
        u.role = SystemRole.USER;

        users.save(u);

        // Build token
        return jwt.generateToken(
                u.email,
                Map.of("role", u.role.name(), "uid", u.id)
        );
    }

    public String login(String email, String rawPassword) {
        User u = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, u.password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return jwt.generateToken(
                u.email,
                Map.of("role", u.role.name(), "uid", u.id)
        );
    }
}