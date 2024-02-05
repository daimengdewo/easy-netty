package com.awen.energy.protocol.message;

import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class SequentialQueue {
    private final ExecutorService executor;

    public SequentialQueue() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void addTask(Runnable task) {
        executor.submit(() -> {
            task.run();
            // 在任务完成后再处理下一个请求
            synchronized (SequentialQueue.this) {
                SequentialQueue.this.notifyAll();
            }
        });
    }
}
