package com.saxion.proj.tfms.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test application class for delivery optimization service tests
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.saxion.proj.tfms.delivery", "com.saxion.proj.tfms.truck"})
@EntityScan(basePackages = {"com.saxion.proj.tfms.delivery.model", "com.saxion.proj.tfms.truck.model"})
@EnableJpaRepositories(basePackages = {"com.saxion.proj.tfms.delivery.repository", "com.saxion.proj.tfms.truck.repository"})
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
