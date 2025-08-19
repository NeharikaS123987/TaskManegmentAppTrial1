package com.example.taskmanager.repo;

import com.example.taskmanager.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, TaskRepositoryCustom {
    List<Task> findByListId(Long listId);
    Page<Task> findByListBoardId(Long boardId, Pageable pageable);
    java.util.List<com.example.taskmanager.domain.Task> findByDueDateAndStatusNot(
            java.time.LocalDate dueDate,
            com.example.taskmanager.domain.TaskStatus status
    );
}
