package com.qudus.docket_backend.auth;

import com.qudus.docket_backend.ratelimit.RateLimiterService;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class VerificationService {
    private final StringRedisTemplate redisTemplate;
    private static final String VERIFICATION_PREFIX = "otp:";
    private static final long OTP_VALID_MINUTES =  15;
    private static final RateLimiterConfig SEND_CONFIG = RateLimiterConfig.custom().limitForPeriod(1).limitRefreshPeriod(Duration.ofMinutes(1)).timeoutDuration(Duration.ZERO).build();
    private static final RateLimiterConfig VERIFY_CONFIG = RateLimiterConfig.custom().limitForPeriod(5).limitRefreshPeriod(Duration.ofMinutes(2)).timeoutDuration(Duration.ZERO).build();
    private final RateLimiterService limiterService;

    public  VerificationService(StringRedisTemplate redisTemplate, RateLimiterService limiterService) {
        this.redisTemplate = redisTemplate;
        this.limiterService = limiterService;
    }

    public void saveVerificationCode(String target, String code) {
        limiterService.allowRequest("verification-send", SEND_CONFIG);
        String key = VERIFICATION_PREFIX + target;

        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(OTP_VALID_MINUTES));
    }

    public boolean isValid(String target, String inputCode) {
        limiterService.allowRequest("verification-verify", VERIFY_CONFIG);
        String key = VERIFICATION_PREFIX + target;
        String savedCode = redisTemplate.opsForValue().get(key);

        if(savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

}
