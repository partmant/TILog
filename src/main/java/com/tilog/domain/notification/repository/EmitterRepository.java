package com.tilog.domain.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(Long memberId, SseEmitter sseEmitter) {
        emitters.put(memberId, sseEmitter);
        return sseEmitter;
    }

    public void deleteById(Long memberId) {
        emitters.remove(memberId);
    }

    public SseEmitter get(Long memberId) {
        return emitters.get(memberId);
    }
}