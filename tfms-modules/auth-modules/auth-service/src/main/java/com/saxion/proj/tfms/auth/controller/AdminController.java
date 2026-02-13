package com.saxion.proj.tfms.auth.controller;

import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import org.springframework.web.bind.annotation.*;

import com.saxion.proj.tfms.auth.abstraction.IUserService;
import com.saxion.proj.tfms.auth.dto.CreateUserDto;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.swagger.SwaggerAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IUserService userService;

    // List all users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> listUsers(@CurrentUser UserContext user) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }
        String role = user.getRole();
        if(!Objects.equals(role, "ADMIN")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Create a new user
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@CurrentUser UserContext user, @RequestBody CreateUserDto dto) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }
        String role = user.getRole();
        if(!Objects.equals(role, "ADMIN")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }
        return ResponseEntity.ok(userService.createUser(dto));
    }

    // Delete a user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@CurrentUser UserContext user, @PathVariable Long userId) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        String role = user.getRole();
        if(!Objects.equals(role, "ADMIN")){
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("Not Authorized"));
        }

        return ResponseEntity.ok(userService.deleteUser(userId));
    }
}
