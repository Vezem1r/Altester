package com.altester.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/health")
@RestController
@Slf4j
public class HealthController {
    @GetMapping("/check")
    public String check(){
        log.info("Received health check");
        return "up";
    }
}
