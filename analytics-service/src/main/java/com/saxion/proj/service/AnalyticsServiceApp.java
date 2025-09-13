package com.saxion.proj.service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyticsServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApp.class, args);
        int x=0;
        x++;
        System.out.println("Value of x: " + x);
    }

}
