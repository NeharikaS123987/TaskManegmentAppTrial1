package com.example.taskmanager.service;

import com.example.taskmanager.domain.SystemRole;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.AnalyticsRepository;
import com.example.taskmanager.repo.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AnalyticsService {
    private final AnalyticsRepository analytics;
    private final BoardRepository boards;

    public AnalyticsService(AnalyticsRepository analytics, BoardRepository boards) {
        this.analytics = analytics;
        this.boards = boards;
    }

    private void ensureAdmin(User current) {
        if (current == null || current.role != SystemRole.ADMIN) {
            throw new SecurityException("Admin only");
        }
    }

    public Map<String, Object> boardSummary(User admin, Long boardId) {
        ensureAdmin(admin);
        var board = boards.findById(boardId).orElseThrow(() -> new IllegalArgumentException("Board not found"));

        var counts = analytics.totalTasksByStatus(boardId);
        var topUsers = analytics.mostActiveUsers(boardId, 5);
        var avgHours = analytics.avgCompletionHours(boardId);

        return Map.of(
                "boardId", board.id,
                "name", board.name,
                "metrics", counts,
                "mostActiveUsers", topUsers,
                "avgCompletionHours", avgHours
        );
    }
}