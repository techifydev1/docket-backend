package com.qudus.docket_backend.auth;

import com.qudus.docket_backend.exceptions.ResponseException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationLimiterService {
    private final HttpServletRequest httpServletRequest;
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private final RateLimiterConfig config = RateLimiterConfig.custom().limitForPeriod(1).limitRefreshPeriod(Duration.ofMinutes(1)).timeoutDuration(Duration.ZERO).build();

    public VerificationLimiterService(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public void allowRequest(String methodName) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        if(limiters.containsKey(ipAddress + methodName)) {
            if(!limiters.get(ipAddress).acquirePermission()) {
                throw new ResponseException("Too many request, please try again later", HttpStatus.TOO_MANY_REQUESTS, "too_many_request");
            }
        }
        RateLimiter newLimiter = RateLimiter.of(ipAddress + methodName, config);
        limiters.put(ipAddress, newLimiter);
    }
}
