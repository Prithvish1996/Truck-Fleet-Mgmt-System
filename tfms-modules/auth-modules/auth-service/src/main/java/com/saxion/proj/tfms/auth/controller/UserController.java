package com.saxion.proj.tfms.auth.controller;

import com.saxion.proj.tfms.auth.abstraction.IUserService;
import com.saxion.proj.tfms.auth.dto.UpdateUserDto;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.security.UserContext;
import com.saxion.proj.tfms.commons.security.annotations.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@CurrentUser UserContext user, @PathVariable Long userId) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @CurrentUser UserContext user,
            @PathVariable Long userId,
            @RequestBody UpdateUserDto updateUserDto) {
        if (!user.isValid()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid token"));
        }

        return ResponseEntity.ok(userService.updateUser(userId, updateUserDto));
    }
}
