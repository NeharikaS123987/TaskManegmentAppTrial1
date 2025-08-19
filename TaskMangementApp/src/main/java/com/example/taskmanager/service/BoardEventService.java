package com.example.taskmanager.service;

import com.example.taskmanager.domain.ActivityType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BoardEventService {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long boardId) {
        SseEmitter emitter = new SseEmitter(0L); // never timeout
        emitters.computeIfAbsent(boardId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        try {
            emitter.send(SseEmitter.event().name("INIT").data("connected").reconnectTime(3000));
        } catch (IOException ignored) {}
        emitter.onCompletion(() -> remove(boardId, emitter));
        emitter.onTimeout(() -> remove(boardId, emitter));
        emitter.onError(e -> remove(boardId, emitter));
        return emitter;
    }

    public void broadcast(Long boardId, ActivityType type, String detail) {
        var list = emitters.get(boardId);
        if (list == null) return;
        for (SseEmitter emitter : list) {
            try {
                var payload = Map.of("type", type.name(), "detail", detail);
                emitter.send(SseEmitter.event()
                        .name("activity")
                        .data(payload, MediaType.APPLICATION_JSON)
                        .reconnectTime(3000));
            } catch (IOException e) {
                remove(boardId, emitter);
            }
        }
    }

    private void remove(Long boardId, SseEmitter emitter) {
        var list = emitters.get(boardId);
        if (list != null) list.remove(emitter);
    }
}