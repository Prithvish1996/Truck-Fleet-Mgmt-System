package com.saxion.proj.tfms.auth.controller;

import org.springframework.web.bind.annotation.*;

import com.saxion.proj.tfms.auth.abstraction.IUserService;
import com.saxion.proj.tfms.auth.dto.CreateUserDto;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.swagger.SwaggerAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private IUserService userService;

    // List all users
    @SwaggerAnnotations.PublicApiOperation(
            summary = "Get all users",
            description = "Get all available users")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Create a new user
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@RequestBody CreateUserDto dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    // Delete a user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
