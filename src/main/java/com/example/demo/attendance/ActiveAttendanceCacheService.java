package com.example.demo.attendance;

import com.example.demo.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ActiveAttendanceCacheService {
    private static final Logger log = LoggerFactory.getLogger(ActiveAttendanceCacheService.class);
    private final RedisTemplate<String, AttendanceDtos.ActiveAttendanceDto> redisTemplate;

    public ActiveAttendanceCacheService(RedisTemplate<String, AttendanceDtos.ActiveAttendanceDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(AttendanceDtos.ActiveAttendanceDto dto) {
        try {
            redisTemplate.opsForValue().set(key(dto.workerId()), dto, Duration.ofHours(16));
        } catch (Exception ex) {
            log.warn("Redis put failed: {}", ex.getMessage());
        }
    }

    public void remove(Long workerId) {
        try {
            redisTemplate.delete(key(workerId));
        } catch (Exception ex) {
            log.warn("Redis delete failed: {}", ex.getMessage());
        }
    }

    public List<AttendanceDtos.ActiveAttendanceDto> getAllActive() {
        try {
            Set<String> keys = redisTemplate.keys("attendance:active:*");
            if (keys == null || keys.isEmpty()) return List.of();
            List<AttendanceDtos.ActiveAttendanceDto> result = new ArrayList<>();
            for (String key : keys) {
                AttendanceDtos.ActiveAttendanceDto dto = redisTemplate.opsForValue().get(key);
                if (Objects.nonNull(dto)) result.add(dto);
            }
            return result;
        } catch (DataAccessException ex) {
            throw new ApiException("REDIS_UNAVAILABLE", "Active attendance cache is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String key(Long workerId) {
        return "attendance:active:" + workerId;
    }
}
