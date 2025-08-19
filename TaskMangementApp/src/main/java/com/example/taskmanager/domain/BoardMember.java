package com.example.taskmanager.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "board_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_board_user", columnNames = {"board_id", "user_id"})
)
public class BoardMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    public Board board;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public BoardRole role = BoardRole.VIEWER;
}