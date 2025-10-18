package com.saxion.proj.tfms;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.saxion.proj.tfms",           // Main application
    "com.saxion.proj.tfms.commons",   // Commons components
    "com.saxion.proj.tfms.auth"       // Auth service (controllers, services, abstractions)
})
public class TfmsApplication {
    public static void main(String[] args) {
        // Set logging context BEFORE Spring Boot starts
        ThreadContext.put("service", "tfms-truck-fleet-management-system");
        ThreadContext.put("environment", System.getProperty("app.environment", "dev"));
        ThreadContext.put("application", "tfms-truck-fleet-management-system");
        
        SpringApplication.run(TfmsApplication.class, args);
    }
}