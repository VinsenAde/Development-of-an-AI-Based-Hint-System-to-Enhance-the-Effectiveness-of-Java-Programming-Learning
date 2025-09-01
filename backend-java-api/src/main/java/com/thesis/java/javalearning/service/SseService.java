package com.thesis.java.javalearning.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(UUID jobId, SseEmitter emitter) {
        emitters.put(jobId, emitter);
    }

    public void removeEmitter(UUID jobId) {
        emitters.remove(jobId);
    }

    public void sendResult(UUID jobId, Object resultPayload) {
        SseEmitter emitter = emitters.get(jobId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("execution-result")
                    .data(resultPayload));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
            removeEmitter(jobId);
        }
    }
}
