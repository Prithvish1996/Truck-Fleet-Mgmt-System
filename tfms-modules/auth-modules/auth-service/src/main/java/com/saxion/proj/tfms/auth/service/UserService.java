package com.saxion.proj.tfms.auth.service;

import com.saxion.proj.tfms.auth.abstraction.IUserService;
import com.saxion.proj.tfms.auth.dto.CreateUserDto;
import com.saxion.proj.tfms.commons.dto.UserDto;
import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.auth.dto.UpdateUserDto;
import com.saxion.proj.tfms.auth.repository.AuthUserRepository;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Service
public class UserService implements IUserService {
    @Autowired
    private AuthUserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public ApiResponse<UserDto> getUserById(Long userId) {
        Optional<UserDao> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found with id: " + userId);
        }

        UserDao user = userOpt.get();
        UserDto dto = UserDto.fromEntity(user);
        return ApiResponse.success(dto);
    }

    @Override
    public ApiResponse<UserDto> updateUser(Long userId, UpdateUserDto updateUserDto) {
        Optional<UserDao> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found with id: " + userId);
        }

        UserDao user = userOpt.get();

        if (updateUserDto.getUsername() != null) {
            user.setUsername(updateUserDto.getUsername());
        }
        if (updateUserDto.getEmail() != null) {
            user.setEmail(updateUserDto.getEmail());
        }
        if (updateUserDto.getUserType() != null) {
            user.setUserType(updateUserDto.getUserType());
        }

        userRepository.save(user);
        return ApiResponse.success(UserDto.fromEntity(user));
    }

    // ===================== ADMIN METHODS =====================

    @Override
    public ApiResponse<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<UserDto> createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ApiResponse.error("Email already exists");
        }

        UserDao user = new UserDao();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setUserType(dto.getUserType());
        user.setActive(true);
        //To be commented if JPA @PrePersist works
        user.setCreatedAt(ZonedDateTime.now(java.time.ZoneId.of("UTC")));
        user.setUpdatedAt(ZonedDateTime.now(java.time.ZoneId.of("UTC")));

        // handle password encoding if configured
        if (passwordEncoder != null && dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            user.setPassword(dto.getPassword());
        }

        userRepository.save(user);
        return ApiResponse.success(UserDto.fromEntity(user));
    }

    @Override
    public ApiResponse<String> deleteUser(Long userId) {
        Optional<UserDao> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found with id: " + userId);
        }

        userRepository.delete(userOpt.get());
        return ApiResponse.success("User deleted successfully");
    }
}
