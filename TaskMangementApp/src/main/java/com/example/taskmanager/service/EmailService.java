package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task;
import com.example.taskmanager.domain.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    public void sendTaskAssignment(User to, Task task) {
        if (to == null || to.email == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to.email);
        msg.setSubject("You were assigned: " + task.title);
        msg.setText("Hi " + to.name + ",\n\n" +
                "You've been assigned to the task '" + task.title + "'.\n" +
                (task.dueDate != null ? ("Due date: " + task.dueDate + "\n") : "") +
                "\n— Task Manager");
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }

    public void sendDueReminder(User to, Task task) {
        if (to == null || to.email == null) return;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to.email);
        msg.setSubject("Reminder: Task due soon — " + task.title);
        msg.setText("Hi " + to.name + ",\n\n" +
                "Your task '" + task.title + "' is due on " + task.dueDate + ".\n" +
                "Please make sure it's on track.\n\n— Task Manager");
        try { mailSender.send(msg); } catch (Exception ignored) {}
    }
}