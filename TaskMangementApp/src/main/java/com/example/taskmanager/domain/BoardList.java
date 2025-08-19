package com.example.taskmanager.domain;

import jakarta.persistence.*;

@Entity
public class BoardList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_id")
    public Board board;
}