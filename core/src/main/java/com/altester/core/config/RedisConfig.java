package com.altester.core.config;

import com.altester.core.dtos.core_service.AdminPage.AdminPageDTO;
import com.altester.core.dtos.core_service.subject.GroupDTO;
import com.altester.core.dtos.core_service.subject.GroupStudentsResponseDTO;
import com.altester.core.util.CacheablePage;
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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
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
        cacheConfigurations.put("students", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        cacheConfigurations.put("teachers", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        cacheConfigurations.put("adminStats", createCacheConfiguration(
                new JsonRedisSerializer<>(AdminPageDTO.class)));

        // Group service caches
        cacheConfigurations.put("groups", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        cacheConfigurations.put("group", createCacheConfiguration(
                new JsonRedisSerializer<>(GroupDTO.class)));

        cacheConfigurations.put("groupStudents", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        cacheConfigurations.put("groupTeachers", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        cacheConfigurations.put("groupStudentsWithCategories", createCacheConfiguration(
                new JsonRedisSerializer<>(GroupStudentsResponseDTO.class)));

        cacheConfigurations.put("groupStudentsNotInGroup", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        // Subject service caches
        cacheConfigurations.put("subjects", createCacheConfiguration(
                new JsonRedisSerializer<>(CacheablePage.class)));

        RedisCacheConfiguration defaultConfig = createCacheConfiguration(
                new JsonRedisSerializer<>(Object.class));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    private RedisCacheConfiguration createCacheConfiguration(JsonRedisSerializer<?> serializer) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisTTL))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();
    }
}