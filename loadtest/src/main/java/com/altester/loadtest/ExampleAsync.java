package com.altester.loadtest;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ExampleAsync {
    @Async
    public CompletableFuture<String> async() throws InterruptedException {
        Thread.sleep(1000);
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }
}
