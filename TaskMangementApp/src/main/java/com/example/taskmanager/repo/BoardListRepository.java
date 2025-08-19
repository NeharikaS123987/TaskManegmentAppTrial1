package com.example.taskmanager.repo;

import com.example.taskmanager.domain.BoardList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardListRepository extends JpaRepository<BoardList, Long> {
    List<BoardList> findByBoardId(Long boardId);
}