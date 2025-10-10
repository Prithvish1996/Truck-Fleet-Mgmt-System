package com.saxion.proj.tfms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simple")
public class SimpleTestController {

    @GetMapping("/test")
    public String simpleTest() {
        return "Simple test endpoint";
    }
}
