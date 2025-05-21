package com.altester.loadtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class Runner implements CommandLineRunner {
    private final ExampleAsync exampleAsync;

    @Override
    public void run(String... args) throws Exception {
        long start = System.currentTimeMillis();

        CompletableFuture<String> f1 = exampleAsync.async();
        CompletableFuture<String> f2 = exampleAsync.async();
        CompletableFuture<String> f3 = exampleAsync.async();

        CompletableFuture.allOf(f1, f2, f3).join();

        log.info("Elapsed time: {}", (System.currentTimeMillis() - start));
        log.info("--> {}", f1.get());
        log.info("--> {}", f2.get());
        log.info("--> {}", f3.get());
    }
}
