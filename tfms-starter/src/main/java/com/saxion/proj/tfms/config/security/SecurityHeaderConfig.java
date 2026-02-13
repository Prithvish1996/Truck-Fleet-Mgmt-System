package com.saxion.proj.tfms.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Security configuration for additional protection
 */
@Configuration
public class SecurityHeaderConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeaderInterceptor())
                .addPathPatterns("/**")  // Apply to all paths
                .order(0);  // Highest priority
    }

    /**
     * Interceptor to add security headers that help prevent information disclosure
     */
    public static class SecurityHeaderInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
            return true; // Let the request proceed
        }
        
        @Override
        public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @org.springframework.lang.Nullable Exception ex) {
            // Add security headers AFTER Spring Security processing to ensure they're not overridden
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            
            // Modern security headers for enhanced protection
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
            response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");
            
            // Enhanced cache control for sensitive endpoints
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // Hide server information
            response.setHeader("Server", "TFMS-Server");
        }
    }
}
