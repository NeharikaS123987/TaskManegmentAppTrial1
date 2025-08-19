package com.example.taskmanager.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "board_activity")
public class BoardActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    public Board board;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    public User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    public ActivityType type;

    @Column(length = 4000)
    public String detail; // free text or small JSON

    @Column(nullable = false, updatable = false)
    public Instant createdAt;

    @PrePersist
    public void onCreate() { this.createdAt = Instant.now(); }
}