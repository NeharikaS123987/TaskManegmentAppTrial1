package com.example.taskmanager.repo;

import com.example.taskmanager.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TaskRepositoryCustom {
    Page<Task> searchInBoard(Long boardId,
                             String query,
                             String status,
                             Long assigneeId,
                             LocalDate dueAfter,
                             LocalDate dueBefore,
                             Pageable pageable);
}