package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.TaskStatus;
import com.example.taskmanager.domain.User;
import com.example.taskmanager.repo.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReminderScheduler {
    private final TaskRepository tasks;
    private final EmailService email;

    public ReminderScheduler(TaskRepository tasks, EmailService email) {
        this.tasks = tasks; this.email = email;
    }

    // Every day at 09:00 server time
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDueTomorrowReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        for (Task t : tasks.findByDueDateAndStatusNot(tomorrow, TaskStatus.DONE)) {
            if (t.assignees == null || t.assignees.isEmpty()) continue;
            for (User u : t.assignees) {
                email.sendDueReminder(u, t);
            }
        }
    }
}