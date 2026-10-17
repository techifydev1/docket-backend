package com.qudus.docket_backend.ratelimit;

import com.qudus.docket_backend.exceptions.ResponseException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final HttpServletRequest httpServletRequest;
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimiterService(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public void allowRequest(String bucket, RateLimiterConfig config) {
        String key = httpServletRequest.getRemoteAddr() + ":" + bucket;
        RateLimiter limiter = limiters.computeIfAbsent(key, k -> RateLimiter.of(k, config));
        if (!limiter.acquirePermission()) {
            throw new ResponseException("Too many request, please try again later", HttpStatus.TOO_MANY_REQUESTS, "too_many_request");
        }
    }
}
