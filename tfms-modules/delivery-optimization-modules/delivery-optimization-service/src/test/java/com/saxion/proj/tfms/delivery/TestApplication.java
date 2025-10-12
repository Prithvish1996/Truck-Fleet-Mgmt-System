package com.saxion.proj.tfms.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test application class for delivery optimization service tests
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.saxion.proj.tfms.delivery")
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
