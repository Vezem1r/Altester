package com.altester.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/penis")
class PenisController {
    private final AppConfiguration appConfiguration;
    private final RestTemplate restTemplate;

    @GetMapping("/health")
    public String getHealth() {
        String url = appConfiguration.getAuthServiceUrl() + "/health/check";
        System.out.println(url);
        return restTemplate.getForObject(url, String.class);
    }
};
