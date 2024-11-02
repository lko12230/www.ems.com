package com.ayush.ems.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

@Component
public class MyBackgroundService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PreDestroy
    public void onDestroy() {
        executor.shutdown(); // Stop accepting new tasks
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Force shutdown if tasks take too long
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
