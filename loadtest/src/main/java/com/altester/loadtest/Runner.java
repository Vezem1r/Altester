package com.altester.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {
    private final ExampleAsync exampleAsync;

    @Override
    public void run(String... args) throws Exception {
        long start = System.currentTimeMillis();

        CompletableFuture<String> f1 = exampleAsync.async();
        CompletableFuture<String> f2 = exampleAsync.async();
        CompletableFuture<String> f3 = exampleAsync.async();

        CompletableFuture.allOf(f1, f2, f3).join();

        System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
        System.out.println("--> " + f1.get());
        System.out.println("--> " + f2.get());
        System.out.println("--> " + f3.get());
    }
}
