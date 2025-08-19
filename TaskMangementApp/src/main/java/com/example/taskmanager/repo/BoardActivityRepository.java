package com.example.taskmanager.repo;

import com.example.taskmanager.domain.BoardActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardActivityRepository extends JpaRepository<BoardActivity, Long> {
    Page<BoardActivity> findByBoardIdOrderByIdDesc(Long boardId, Pageable pageable);
}