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
import com.saxion.proj.tfms.commons.logging.ServiceLogger;
import com.saxion.proj.tfms.commons.logging.ServiceName;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AuthService implements IAuthService {

    private static final ServiceLogger logger = ServiceLogger.getLogger(AuthService.class);

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public ApiResponse<LoginResponseDto> authenticate(LoginRequestDto request) {
        try {
            logger.infoOp(ServiceName.SECURITY_SERVICE, "LOGIN", "Authentication attempt for user: {}", request.getEmail());

            UserDao user = getUser(request);
            String token = jwtUtil.generateTokenWithId(user.getEmail(), user.getUserType().name(), user.getId());
            LoginResponseDto responseData = getLoginResponseDto(token, user);
            
            logger.infoOp(ServiceName.SECURITY_SERVICE, "LOGIN", "Authentication successful for user: {}", request.getEmail());
            return getApiResponse(responseData);
            
        } catch (UserNotFoundException | InvalidCredentialsException e) {
            logger.warnOp(ServiceName.SECURITY_SERVICE, "LOGIN", "Authentication failed for user: {} - {}", request.getEmail(), e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.errorOp(ServiceName.SECURITY_SERVICE, "LOGIN", "Authentication error for user: {}", e, request.getEmail());
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
