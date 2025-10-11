package com.saxion.proj.tfms.auth.abstraction;

import com.saxion.proj.tfms.auth.dto.CreateUserDto;
import com.saxion.proj.tfms.auth.dto.UpdateUserDto;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.dto.UserDto;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface IUserService {
    ApiResponse<UserDto> getUserById(Long userId);
    ApiResponse<UserDto> updateUser(Long userId, UpdateUserDto updateUserDto);

    // Admin-only methods
    ApiResponse<List<UserDto>> getAllUsers();
    ApiResponse<UserDto> createUser(CreateUserDto createUserDto);
    ApiResponse<String> deleteUser(Long userId);
}
