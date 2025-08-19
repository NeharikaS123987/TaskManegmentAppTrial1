package com.example.taskmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false) // will store the BCrypt hash
    public String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SystemRole role = SystemRole.USER;
}