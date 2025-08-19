package com.example.taskmanager.service;

import com.example.taskmanager.domain.Board;
import com.example.taskmanager.domain.BoardList;
import com.example.taskmanager.repo.BoardListRepository;
import com.example.taskmanager.repo.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardListService {

    private final BoardListRepository lists;
    private final BoardRepository boards;

    public BoardListService(BoardListRepository lists, BoardRepository boards) {
        this.lists = lists;
        this.boards = boards;
    }

    public List<BoardList> getLists(Long boardId) {
        return lists.findByBoardId(boardId);
    }

    @Transactional
    public BoardList createList(Long boardId, String name) {
        Board board = boards.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("Board not found"));

        BoardList list = new BoardList();
        list.name = name;
        list.board = board;
        return lists.save(list);
    }

    @Transactional
    public BoardList updateList(Long id, String name) {
        BoardList list = lists.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("List not found"));
        list.name = name;
        return lists.save(list);
    }

    @Transactional
    public void deleteList(Long id) {
        lists.deleteById(id);
    }
}