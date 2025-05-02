package com.altester.ai_grading_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiServiceConfig {

    private OpenAi openai = new OpenAi();
    private Anthropic anthropic = new Anthropic();
    private Deepseek deepseek = new Deepseek();
    private Gemini gemini = new Gemini();

    @Setter
    @Getter
    public static class OpenAi {
        private String model = "gpt-4o";
        private int timeout = 30;

    }

    @Setter
    @Getter
    public static class Anthropic {
        private String model = "claude-3-haiku-20240307";
        private int timeout = 30;

    }

    @Setter
    @Getter
    public static class Deepseek {
        private String model = "deepseek-chat";
        private int timeout = 30;

    }

    @Setter
    @Getter
    public static class Gemini {
        private String model = "gemini-pro";
        private int timeout = 30;

    }

}