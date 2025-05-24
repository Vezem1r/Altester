package com.altester.loadtest;

import com.altester.client.api.AuthControllerApi;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExampleAsync {
    private final AuthControllerApi authController;

    @Async
    public CompletableFuture<String> async() {
        Map<String, Object> authConfig = authController.getAuthConfig();
        return CompletableFuture.completedFuture(Thread.currentThread().getName() + ": " + authConfig.get("mode"));
    }
}
