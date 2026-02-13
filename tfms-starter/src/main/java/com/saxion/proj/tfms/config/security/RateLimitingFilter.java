package com.saxion.proj.tfms.config.security;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    
    public RateLimitingFilter() {
        // Reset counters every minute
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
            requestCounts::clear, 1, 1, TimeUnit.MINUTES
        );
        logger.info("Rate limiting filter initialized with limit: {} requests per minute", MAX_REQUESTS_PER_MINUTE);
    }
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        
        String clientIp = getClientIP(request);
        AtomicInteger requests = requestCounts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        int currentCount = requests.incrementAndGet();
        
        logger.debug("Rate limit check: IP={}, Count={}, Limit={}", clientIp, currentCount, MAX_REQUESTS_PER_MINUTE);
        
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            logger.warn("Rate limit exceeded for IP: {} (Count: {})", clientIp, currentCount);
            response.setStatus(429);
            response.setContentType("application/json");
            // Generic error message - don't reveal specifics to attackers
            response.getWriter().write("{\"error\":\"Too many requests\",\"message\":\"Please try again later\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}