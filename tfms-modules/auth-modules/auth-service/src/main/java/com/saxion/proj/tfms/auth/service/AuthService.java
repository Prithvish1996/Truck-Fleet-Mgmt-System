package com.saxion.proj.tfms.auth.service;

import com.saxion.proj.tfms.auth.abstraction.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.saxion.proj.tfms.auth.dto.LoginRequestDto;
import com.saxion.proj.tfms.auth.dto.LoginResponseDto;
import com.saxion.proj.tfms.auth.repository.AuthUserRepository;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.security.JwtUtil;
import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.commons.exception.auth.UserNotFoundException;
import com.saxion.proj.tfms.commons.exception.auth.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AuthService implements IAuthService {

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public ApiResponse<LoginResponseDto> authenticate(LoginRequestDto request) {
        try {

            UserDao user = getUser(request);
            String token = jwtUtil.generateToken(user.getEmail(), user.getUserType().name());
            LoginResponseDto responseData = getLoginResponseDto(token, user);
            return getApiResponse(responseData);
            
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Authentication failed: " + e.getMessage());
        }
    }




    private UserDao getUser(LoginRequestDto request) {
        UserDao user = userRepository.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Authentication failed"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Authentication failed");
        }
        return user;
    }

    private static ApiResponse<LoginResponseDto> getApiResponse(LoginResponseDto responseData) {
        return ApiResponse.success(responseData, "Authentication successful");
    }

    private LoginResponseDto getLoginResponseDto(String token, UserDao user) {
        return LoginResponseDto.builder()
                .accessToken(token)
                .refreshToken(null)
                .expiresIn(jwtUtil.getExpirationTime())
                .username(user.getEmail())
                .userType(user.getUserType().name())
                .email(user.getEmail())
                .success(true)
                .message("Login successful")
                .build();
    }


}
