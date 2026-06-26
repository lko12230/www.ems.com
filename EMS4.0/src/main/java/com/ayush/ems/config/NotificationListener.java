package com.ayush.ems.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ayush.ems.entities.Notification;

@Component
public class NotificationListener {

    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public static void register(String username, SseEmitter emitter) {
        emitters.put(username, emitter);
        System.out.println("✅ SSE connection established for: " + username);
    }

    public static void remove(String username) {
        emitters.remove(username);
        System.out.println("❌ SSE connection removed for: " + username);
    }

    public static void send(String username, Notification notification) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            } catch (IOException e) {
                emitter.completeWithError(e);
                remove(username);
            }
        } else {
            System.out.println("⚠️ No active emitter for: " + username);
        }
    }
}
