package com.ween.security;

// Redis Token Service (DISABLED - Redis is disabled)
/*
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";
    private static final String RESET_TOKEN_PREFIX = "token:reset:";
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    public void blacklistToken(String jti, long expirySeconds) {
        String key = BLACKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "true", expirySeconds, TimeUnit.SECONDS);
        log.debug("Token blacklisted: {}", jti);
    }

    public boolean isTokenBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void storeRefreshToken(String userId, String token, long expirySeconds) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expirySeconds, TimeUnit.SECONDS);
        log.debug("Refresh token stored for user: {}", userId);
    }

    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh token deleted for user: {}", userId);
    }

    public void storeResetToken(String resetToken, String email, long expirySeconds) {
        String key = RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.opsForValue().set(key, email, expirySeconds, TimeUnit.SECONDS);
        log.debug("Reset token stored for email: {}", email);
    }

    public String getResetTokenEmail(String resetToken) {
        String key = RESET_TOKEN_PREFIX + resetToken;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteResetToken(String resetToken) {
        String key = RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.delete(key);
        log.debug("Reset token deleted");
    }

    public void incrementRateLimit(String key, long expirySeconds) {
        String limitKey = RATE_LIMIT_PREFIX + key;
        Long count = redisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            redisTemplate.expire(limitKey, expirySeconds, TimeUnit.SECONDS);
        }
    }

    public long getRateLimitCount(String key) {
        String limitKey = RATE_LIMIT_PREFIX + key;
        return Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(limitKey)));
    }
}
*/
