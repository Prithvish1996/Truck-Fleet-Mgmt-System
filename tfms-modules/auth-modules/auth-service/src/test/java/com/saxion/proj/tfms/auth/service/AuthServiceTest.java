package com.saxion.proj.tfms.auth.service;

import com.saxion.proj.tfms.auth.dto.LoginRequestDto;
import com.saxion.proj.tfms.auth.dto.LoginResponseDto;
import com.saxion.proj.tfms.auth.repository.AuthUserRepository;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.model.UserDao;
import com.saxion.proj.tfms.commons.model.UserDao.UserType;
import com.saxion.proj.tfms.commons.security.JwtUtil;
import com.saxion.proj.tfms.commons.exception.auth.UserNotFoundException;
import com.saxion.proj.tfms.commons.exception.auth.InvalidCredentialsException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private LoginRequestDto validLoginRequest;
    private UserDao validUser;
    private String hashedPassword;
    private String plainPassword;

    @BeforeEach
    void setUp() {
        plainPassword = "Password123";
        hashedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF123456";
        
        validLoginRequest = new LoginRequestDto();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword(plainPassword);
        
        validUser = new UserDao();
        validUser.setId(1L);
        validUser.setEmail("test@example.com");
        validUser.setPassword(hashedPassword);
        validUser.setUserType(UserType.ADMIN);
        validUser.setActive(true);
        validUser.setCreatedAt(ZonedDateTime.now());
        validUser.setUpdatedAt(ZonedDateTime.now());
    }

    
//    @Test
//    void authenticate_WithValidCredentials_ShouldReturnSuccessResponse() {
//
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String properlyHashedPassword = encoder.encode(plainPassword);
//        validUser.setPassword(properlyHashedPassword);
//
//        when(userRepository.findActiveByEmail(validLoginRequest.getEmail()))
//                .thenReturn(Optional.of(validUser));
//        when(jwtUtil.generateToken(validUser.getEmail(), validUser.getUserType().name()))
//                .thenReturn("mock-jwt-token");
//        when(jwtUtil.getExpirationTime()).thenReturn(3600L);
//
//
//        ApiResponse<LoginResponseDto> response = authService.authenticate(validLoginRequest);
//
//
//        assertTrue(response.isSuccess());
//        assertEquals("Authentication successful", response.getMessage());
//        assertNotNull(response.getData());
//
//        LoginResponseDto data = response.getData();
//        assertEquals("mock-jwt-token", data.getAccessToken());
//        assertEquals("Bearer", data.getTokenType());
//        assertEquals(3600L, data.getExpiresIn());
//        assertEquals("test@example.com", data.getUsername());
//        assertEquals("ADMIN", data.getUserType());
//        assertEquals("test@example.com", data.getEmail());
//        assertTrue(data.isSuccess());
//        assertEquals("Login successful", data.getMessage());
//
//        verify(userRepository).findActiveByEmail(validLoginRequest.getEmail());
//        verify(jwtUtil).generateToken(validUser.getEmail(), validUser.getUserType().name());
//        verify(jwtUtil).getExpirationTime();
//    }

  
    @Test
    void authenticate_WithNonExistentUser_ShouldReturnErrorResponse() {
       
        when(userRepository.findActiveByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.empty());

      
        ApiResponse<LoginResponseDto> response = authService.authenticate(validLoginRequest);

        
        // Then
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Authentication failed"));
        assertNull(response.getData());
        
        verify(userRepository).findActiveByEmail(validLoginRequest.getEmail());
        verifyNoInteractions(jwtUtil);
    }

   
    @Test
    void authenticate_WithInvalidPassword_ShouldReturnErrorResponse() {
      
        validLoginRequest.setPassword("WrongPassword123");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String correctHashedPassword = encoder.encode("CorrectPassword123");
        validUser.setPassword(correctHashedPassword);
        
        when(userRepository.findActiveByEmail(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(validUser));

       
        ApiResponse<LoginResponseDto> response = authService.authenticate(validLoginRequest);
        
       
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Authentication failed"));
        assertNull(response.getData());
        
        verify(userRepository).findActiveByEmail(validLoginRequest.getEmail());
        verifyNoInteractions(jwtUtil);
    }

  
//    @Test
//    void authenticate_WithJwtGenerationFailure_ShouldReturnErrorResponse() {
//
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String properlyHashedPassword = encoder.encode(plainPassword);
//        validUser.setPassword(properlyHashedPassword);
//
//        when(userRepository.findActiveByEmail(validLoginRequest.getEmail()))
//                .thenReturn(Optional.of(validUser));
//        when(jwtUtil.generateToken(anyString(), anyString()))
//                .thenThrow(new RuntimeException("JWT generation failed"));
//
//
//        ApiResponse<LoginResponseDto> response = authService.authenticate(validLoginRequest);
//
//
//        assertFalse(response.isSuccess());
//        assertTrue(response.getMessage().contains("Authentication failed: JWT generation failed"));
//        assertNull(response.getData());
//
//        verify(userRepository).findActiveByEmail(validLoginRequest.getEmail());
//        verify(jwtUtil).generateToken(validUser.getEmail(), validUser.getUserType().name());
//    }

    
    @Test
    void authenticate_WithRepositoryException_ShouldReturnErrorResponse() {
        
        when(userRepository.findActiveByEmail(validLoginRequest.getEmail()))
                .thenThrow(new RuntimeException("Database connection failed"));

       
        ApiResponse<LoginResponseDto> response = authService.authenticate(validLoginRequest);

      
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Authentication failed: Database connection failed"));
        assertNull(response.getData());
        
        verify(userRepository).findActiveByEmail(validLoginRequest.getEmail());
        verifyNoInteractions(jwtUtil);
    }

  
    @Test
    void authenticate_WithNullEmail_ShouldReturnErrorResponse() {
       
        LoginRequestDto nullEmailRequest = new LoginRequestDto();
        nullEmailRequest.setEmail(null);
        nullEmailRequest.setPassword(plainPassword);

        when(userRepository.findActiveByEmail(null))
                .thenThrow(new IllegalArgumentException("Email cannot be null"));

        
        ApiResponse<LoginResponseDto> response = authService.authenticate(nullEmailRequest);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Authentication failed: Email cannot be null"));
        assertNull(response.getData());
    }

       
    @Test
    void authenticate_WithEmptyPassword_ShouldReturnErrorResponse() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String properlyHashedPassword = encoder.encode(plainPassword);
        validUser.setPassword(properlyHashedPassword);
        
        LoginRequestDto emptyPasswordRequest = new LoginRequestDto();
        emptyPasswordRequest.setEmail("test@example.com");
        emptyPasswordRequest.setPassword("");

        when(userRepository.findActiveByEmail("test@example.com"))
                .thenReturn(Optional.of(validUser));

       
        ApiResponse<LoginResponseDto> response = authService.authenticate(emptyPasswordRequest);
        
       
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Invalid email or password") || 
                   response.getMessage().contains("Authentication failed"));
        assertNull(response.getData());
        
        verify(userRepository).findActiveByEmail("test@example.com");
    }

  
//    @Test
//    void authenticate_WithDriverUser_ShouldReturnSuccessResponse() {
//
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        String properlyHashedPassword = encoder.encode(plainPassword);
//
//        UserDao driverUser = new UserDao();
//        driverUser.setId(2L);
//        driverUser.setEmail("driver@example.com");
//        driverUser.setPassword(properlyHashedPassword);
//        driverUser.setUserType(UserType.DRIVER);
//        driverUser.setActive(true);
//        driverUser.setCreatedAt(ZonedDateTime.now());
//        driverUser.setUpdatedAt(ZonedDateTime.now());
//
//        LoginRequestDto driverRequest = new LoginRequestDto();
//        driverRequest.setEmail("driver@example.com");
//        driverRequest.setPassword(plainPassword);
//
//        when(userRepository.findActiveByEmail("driver@example.com"))
//                .thenReturn(Optional.of(driverUser));
//        when(jwtUtil.generateToken("driver@example.com", "DRIVER"))
//                .thenReturn("driver-jwt-token");
//        when(jwtUtil.getExpirationTime()).thenReturn(3600L);
//
//
//        ApiResponse<LoginResponseDto> response = authService.authenticate(driverRequest);
//
//
//        assertTrue(response.isSuccess());
//        assertEquals("Authentication successful", response.getMessage());
//        assertNotNull(response.getData());
//        assertEquals("DRIVER", response.getData().getUserType());
//        assertEquals("driver-jwt-token", response.getData().getAccessToken());
//        assertEquals(3600L, response.getData().getExpiresIn());
//
//        verify(userRepository).findActiveByEmail("driver@example.com");
//        verify(jwtUtil).generateToken("driver@example.com", "DRIVER");
//    }
}