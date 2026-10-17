package com.qudus.docket_backend.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class VerificationService {
    private final StringRedisTemplate redisTemplate;
    private static final String VERIFICATION_PREFIX = "otp:";
    private static final long OTP_VALID_MINUTES =  15;
    private final VerificationLimiterService limiterService;

    public  VerificationService(StringRedisTemplate redisTemplate, VerificationLimiterService limiterService) {
        this.redisTemplate = redisTemplate;
        this.limiterService = limiterService;
    }

    public void saveVerificationCode(String target, String code) {
        String methodName = StackWalker.getInstance().walk(frames -> frames.findFirst().map(StackWalker.StackFrame::getMethodName)).orElse("Unknown");
        limiterService.allowRequest(methodName);
        String key = VERIFICATION_PREFIX + target;

        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(OTP_VALID_MINUTES));
    }

    public boolean isValid(String target, String inputCode) {
        String methodName = StackWalker.getInstance().walk(frames -> frames.findFirst().map(StackWalker.StackFrame::getMethodName)).orElse("Unknown");
        limiterService.allowRequest(methodName);
        String key = VERIFICATION_PREFIX + target;
        String savedCode = redisTemplate.opsForValue().get(key);

        if(savedCode != null && savedCode.equals(inputCode)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

}
