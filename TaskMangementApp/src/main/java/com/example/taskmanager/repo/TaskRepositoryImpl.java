package com.example.taskmanager.repo;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom {

    private final EntityManager em;

    public TaskRepositoryImpl(EntityManager em) { this.em = em; }

    @Override
    public Page<Task> searchInBoard(Long boardId, String query, String status, Long assigneeId,
                                    LocalDate dueAfter, LocalDate dueBefore, Pageable pageable) {
        String base = """
            from Task t
            join t.list l
            where l.board.id = :boardId
            """;
        Map<String, Object> params = new HashMap<>();
        params.put("boardId", boardId);

        if (query != null && !query.isBlank()) {
            base += " and (lower(t.title) like :q or lower(t.description) like :q) ";
            params.put("q", "%" + query.toLowerCase() + "%");
        }
        if (status != null && !status.isBlank()) {
            base += " and t.status = :status ";
            params.put("status", TaskStatus.valueOf(status));
        }
        if (assigneeId != null) {
            base += " and exists (select 1 from t.assignees a where a.id = :assigneeId) ";
            params.put("assigneeId", assigneeId);
        }
        if (dueAfter != null) {
            base += " and (t.dueDate is not null and t.dueDate >= :dueAfter) ";
            params.put("dueAfter", dueAfter);
        }
        if (dueBefore != null) {
            base += " and (t.dueDate is not null and t.dueDate <= :dueBefore) ";
            params.put("dueBefore", dueBefore);
        }

        String select = "select t " + base + " order by t.id desc";
        String countQ = "select count(t) " + base;

        TypedQuery<Task> queryQ = em.createQuery(select, Task.class);
        jakarta.persistence.Query countQuery = em.createQuery(countQ);

        params.forEach((k, v) -> {
            queryQ.setParameter(k, v);
            countQuery.setParameter(k, v);
        });

        queryQ.setFirstResult((int) pageable.getOffset());
        queryQ.setMaxResults(pageable.getPageSize());

        List<Task> items = queryQ.getResultList();
        long total = (long) countQuery.getSingleResult();

        return new PageImpl<>(items, pageable, total);
    }
}