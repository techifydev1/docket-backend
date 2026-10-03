package com.qudus.docket_backend.auth;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class VerificationService {
    private final StringRedisTemplate redisTemplate;
    private static final String VERIFICATION_PREFIX = "otp:";
    private static final long OTP_VALID_MINUTES =  15;

    public  VerificationService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveVerificationCode(String target, String code) {
        String key = VERIFICATION_PREFIX + target;

        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(OTP_VALID_MINUTES));
    }

    public boolean isValid(String target, String inputCode) {
        String key = VERIFICATION_PREFIX + target;
        String savedCode = redisTemplate.opsForValue().get(key);

        if(savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

}
