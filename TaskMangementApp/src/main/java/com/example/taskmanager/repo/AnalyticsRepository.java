package com.example.taskmanager.repo;

import com.example.taskmanager.domain.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager em;

    public Map<String, Long> totalTasksByStatus(Long boardId) {
        // total created (all tasks under lists of board), total completed
        Long total = em.createQuery("""
            select count(t) from Task t
            where t.list.board.id = :bid
        """, Long.class).setParameter("bid", boardId).getSingleResult();

        Long completed = em.createQuery("""
            select count(t) from Task t
            where t.list.board.id = :bid and t.status = :done
        """, Long.class).setParameter("bid", boardId)
                .setParameter("done", TaskStatus.DONE).getSingleResult();

        Map<String, Long> out = new LinkedHashMap<>();
        out.put("totalCreated", total);
        out.put("totalCompleted", completed);
        out.put("open", total - completed);
        return out;
    }

    public List<Map<String, Object>> mostActiveUsers(Long boardId, int limit) {
        // Uses activity table; count actions per actor on this board
        List<Object[]> rows = em.createQuery("""
            select a.actorId, count(a.id)
            from BoardActivity a
            where a.board.id = :bid
            group by a.actorId
            order by count(a.id) desc
        """, Object[].class).setParameter("bid", boardId)
                .setMaxResults(limit).getResultList();

        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", r[0]);
            m.put("activityCount", r[1]);
            out.add(m);
        }
        return out;
    }

    public Double avgCompletionHours(Long boardId) {
        // average (completedAt - createdAt) for tasks with completedAt
        List<Object[]> rows = em.createQuery("""
            select t.createdAt, t.completedAt
            from Task t
            where t.list.board.id = :bid and t.completedAt is not null
        """, Object[].class).setParameter("bid", boardId).getResultList();

        if (rows.isEmpty()) return 0.0;
        long totalSecs = 0;
        for (Object[] r : rows) {
            Instant created = (Instant) r[0];
            Instant completed = (Instant) r[1];
            totalSecs += Duration.between(created, completed).getSeconds();
        }
        double avgSecs = (double) totalSecs / rows.size();
        return avgSecs / 3600.0; // hours
    }
}