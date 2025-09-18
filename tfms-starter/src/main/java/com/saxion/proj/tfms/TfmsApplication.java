package com.saxion.proj.tfms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.saxion.proj.tfms")
public class TfmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TfmsApplication.class, args);
    }
}