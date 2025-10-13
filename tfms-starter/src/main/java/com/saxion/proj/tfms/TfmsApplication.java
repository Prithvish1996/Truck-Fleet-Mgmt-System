package com.saxion.proj.tfms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.saxion.proj.tfms",           // Main application
    "com.saxion.proj.tfms.commons",   // Commons components
    "com.saxion.proj.tfms.auth",       // Auth service controllers
    "com.saxion.proj.tfms.auth.abstraction"       // Auth service controllers
    // Temporarily exclude delivery package to resolve Truck entity conflict
    // "com.saxion.proj.tfms.delivery"   // Delivery optimization service
})
public class TfmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TfmsApplication.class, args);
    }
}