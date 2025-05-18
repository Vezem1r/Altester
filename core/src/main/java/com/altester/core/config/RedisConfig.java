package com.altester.core.config;

import com.altester.core.dtos.ai_service.PromptDetailsDTO;
import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherGroupDetailDTO;
import com.altester.core.dtos.core_service.TeacherPage.TeacherPageDTO;
import com.altester.core.dtos.core_service.apiKey.TestApiKeysDTO;
import com.altester.core.dtos.core_service.question.QuestionDetailsDTO;
import com.altester.core.dtos.core_service.retrieval.StudentTestAttemptsResponseDTO;
import com.altester.core.dtos.core_service.student.*;
import com.altester.core.dtos.core_service.subject.GroupDTO;
import com.altester.core.dtos.core_service.subject.GroupStudentsResponseDTO;
import com.altester.core.dtos.core_service.test.TestPreviewDTO;
import com.altester.core.dtos.core_service.test.TestSummaryDTO;
import com.altester.core.util.CacheablePage;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

  @Value("${redis.host}")
  private String redisHost;

  @Value("${redis.port}")
  private int redisPort;

  @Value("${redis.ttl}")
  private long redisTTL;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration configuration =
        new RedisStandaloneConfiguration(redisHost, redisPort);
    return new LettuceConnectionFactory(configuration);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new JsonRedisSerializer<>(Object.class));
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new JsonRedisSerializer<>(Object.class));
    template.afterPropertiesSet();
    return template;
  }

  @Primary
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // Admin page caches
    cacheConfigurations.put(
        "students", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "teachers", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "adminStats", createCacheConfiguration(new JsonRedisSerializer<>(AdminPageDTO.class)));

    // API key caches
    cacheConfigurations.put(
        "apiKeys", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "availableApiKeys", createCacheConfiguration(new JsonRedisSerializer<>(List.class)));

    cacheConfigurations.put(
        "testApiKeys", createCacheConfiguration(new JsonRedisSerializer<>(TestApiKeysDTO.class)));

    // Group service caches
    cacheConfigurations.put(
        "groups", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "group", createCacheConfiguration(new JsonRedisSerializer<>(GroupDTO.class)));

    cacheConfigurations.put(
        "groupStudents", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "groupTeachers", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "groupStudentsWithCategories",
        createCacheConfiguration(new JsonRedisSerializer<>(GroupStudentsResponseDTO.class)));

    cacheConfigurations.put(
        "groupStudentsNotInGroup",
        createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    // Subject service caches
    cacheConfigurations.put(
        "subjects", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    RedisCacheConfiguration defaultConfig =
        createCacheConfiguration(new JsonRedisSerializer<>(Object.class));

    // Test service caches
    cacheConfigurations.put(
        "tests", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "test", createCacheConfiguration(new JsonRedisSerializer<>(TestPreviewDTO.class)));

    cacheConfigurations.put(
        "testSummary", createCacheConfiguration(new JsonRedisSerializer<>(TestSummaryDTO.class)));

    cacheConfigurations.put(
        "testsBySubject", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "testsByGroup", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    // Question service caches
    cacheConfigurations.put(
        "questions", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "question", createCacheConfiguration(new JsonRedisSerializer<>(QuestionDetailsDTO.class)));

    cacheConfigurations.put(
        "testQuestions", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    // TeacherPageService caches
    cacheConfigurations.put(
        "teacherPage", createCacheConfiguration(new JsonRedisSerializer<>(TeacherPageDTO.class)));
    cacheConfigurations.put(
        "teacherStudents",
        createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));
    cacheConfigurations.put(
        "teacherGroups", createCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));
    cacheConfigurations.put(
        "teacherGroup",
        createCacheConfiguration(new JsonRedisSerializer<>(TeacherGroupDetailDTO.class)));

    // StudentService caches
    cacheConfigurations.put(
        "studentDashboard",
        createCacheConfiguration(new JsonRedisSerializer<>(StudentDashboardResponse.class)));
    cacheConfigurations.put(
        "academicHistory",
        createCacheConfiguration(new JsonRedisSerializer<>(AcademicHistoryResponse.class)));
    cacheConfigurations.put(
        "availablePeriods",
        createCacheConfiguration(new JsonRedisSerializer<>(AvailablePeriodsResponse.class)));
    cacheConfigurations.put(
        "studentTestAttempts",
        createCacheConfiguration(new JsonRedisSerializer<>(StudentAttemptsResponse.class)));
    cacheConfigurations.put(
        "attemptReview",
        createCacheConfiguration(new JsonRedisSerializer<>(AttemptReviewDTO.class)));

    // AttemptRetrievalService caches
    cacheConfigurations.put(
        "testAttemptsForTeacher", createCacheConfiguration(new JsonRedisSerializer<>(List.class)));
    cacheConfigurations.put(
        "testAttemptsForAdmin", createCacheConfiguration(new JsonRedisSerializer<>(List.class)));
    cacheConfigurations.put(
        "studentAttemptsForTeacher",
        createCacheConfiguration(new JsonRedisSerializer<>(StudentTestAttemptsResponseDTO.class)));
    cacheConfigurations.put(
        "studentAttemptsForAdmin",
        createCacheConfiguration(new JsonRedisSerializer<>(StudentTestAttemptsResponseDTO.class)));
    cacheConfigurations.put(
        "studentTestAttemptsForTeacher",
        createCacheConfiguration(new JsonRedisSerializer<>(List.class)));
    cacheConfigurations.put(
        "studentTestAttemptsForAdmin",
        createCacheConfiguration(new JsonRedisSerializer<>(List.class)));

    // Prompt service caches
    cacheConfigurations.put(
        "prompts",
        createInfiniteCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "myPrompts",
        createInfiniteCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "publicPrompts",
        createInfiniteCacheConfiguration(new JsonRedisSerializer<>(CacheablePage.class)));

    cacheConfigurations.put(
        "promptDetails",
        createInfiniteCacheConfiguration(new JsonRedisSerializer<>(PromptDetailsDTO.class)));

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigurations)
        .transactionAware()
        .build();
  }

  private RedisCacheConfiguration createCacheConfiguration(JsonRedisSerializer<?> serializer) {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofSeconds(redisTTL))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
        .disableCachingNullValues();
  }

  private RedisCacheConfiguration createInfiniteCacheConfiguration(
      JsonRedisSerializer<?> serializer) {
    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
        .disableCachingNullValues();
  }
}
