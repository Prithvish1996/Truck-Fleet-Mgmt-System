package com.saxion.proj.tfms.config.logging;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Configuration to automatically set the service name in Log4j2 ThreadContext
 * This ensures that log files are created with proper service names instead of ${ctx:service}.log
 */
@Component
public class LoggingConfig {

    @Value("${spring.application.name:tfms-service}")
    private String applicationName;

    static {
        // Set a default service name as early as possible (before Spring starts)
        String defaultService = System.getProperty("spring.application.name", "tfms-service");
        ThreadContext.put("service", sanitizeServiceName(defaultService));
        ThreadContext.put("environment", getEnvironmentStatic());
        ThreadContext.put("application", sanitizeServiceName(defaultService));
    }

    @PostConstruct
    public void configureLogging() {
        // Update with the actual Spring application name once available
        String serviceName = sanitizeServiceName(applicationName);
        ThreadContext.put("service", serviceName);
        
        String environment = System.getProperty("app.environment", 
                           System.getenv("APP_ENV") != null ? System.getenv("APP_ENV") : "dev");
        ThreadContext.put("environment", environment);
        ThreadContext.put("application", serviceName);
        
        System.out.println("📝 Logging configured - Service: " + serviceName + ", Environment: " + environment);
    }

    /**
     * Sanitize application name to create a valid filename
     * Removes special characters and converts to lowercase with hyphens
     */
    private static String sanitizeServiceName(String name) {
        if (name == null || name.isEmpty()) {
            return "tfms-service";
        }
        // Remove special characters and convert to lowercase
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9\\-_]", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }

    private static String getEnvironmentStatic() {
        return System.getProperty("app.environment", 
               System.getenv("APP_ENV") != null ? System.getenv("APP_ENV") : "dev");
    }
}
