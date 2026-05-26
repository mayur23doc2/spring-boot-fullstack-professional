package com.example.demo.common.config;

import com.example.demo.attendance.AttendanceDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    RedisTemplate<String, AttendanceDtos.ActiveAttendanceDto> activeAttendanceRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, AttendanceDtos.ActiveAttendanceDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) { log.warn("Cache get failed {}", exception.getMessage()); }
            @Override public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) { log.warn("Cache put failed {}", exception.getMessage()); }
            @Override public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) { log.warn("Cache evict failed {}", exception.getMessage()); }
            @Override public void handleCacheClearError(RuntimeException exception, Cache cache) { log.warn("Cache clear failed {}", exception.getMessage()); }
        };
    }
}
