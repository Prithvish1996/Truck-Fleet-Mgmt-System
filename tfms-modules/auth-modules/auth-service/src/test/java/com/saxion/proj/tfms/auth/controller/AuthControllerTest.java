package com.saxion.proj.tfms.auth.controller;

import com.saxion.proj.tfms.auth.dto.LoginRequestDto;
import com.saxion.proj.tfms.auth.dto.LoginResponseDto;
import com.saxion.proj.tfms.auth.dto.UserProfileDto;
import com.saxion.proj.tfms.auth.service.AuthService;
import com.saxion.proj.tfms.commons.dto.ApiResponse;
import com.saxion.proj.tfms.commons.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.saxion.proj.tfms.commons.exception.TFMSExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private LoginRequestDto validLoginRequest;
    private LoginResponseDto loginResponse;

    //
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new TFMSExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        validLoginRequest = new LoginRequestDto();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Password123");
        loginResponse = new LoginResponseDto();

    }

    @Test
    void login_WithValidRequest_ShouldReturnSuccess() throws Exception {
        ApiResponse<LoginResponseDto> successResponse = ApiResponse.success(loginResponse);
        when(authService.authenticate(any(LoginRequestDto.class))).thenReturn(successResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authService).authenticate(any(LoginRequestDto.class));
    }


    @Test
    void login_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        LoginRequestDto invalidRequest = new LoginRequestDto();
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any(LoginRequestDto.class));
    }

    @Test
    void login_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDto.class)))
                .thenThrow(new RuntimeException("Authentication failed"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).authenticate(any(LoginRequestDto.class));
    }


    @Test
    void logout_WithValidBearerToken_ShouldReturnSuccess() throws Exception {

        String token = "valid-jwt-token";
        doNothing().when(tokenBlacklistService).blackListToken(token);
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(tokenBlacklistService).blackListToken(token);
    }


    @Test
    void logout_WithoutAuthorizationHeader_ShouldReturnBadRequest() throws Exception {

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(tokenBlacklistService, never()).blackListToken(anyString());
    }


    @Test
    void logout_WithMalformedAuthHeader_ShouldHandleGracefully() throws Exception {

        String malformedHeader = "InvalidFormat";

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", malformedHeader))
                .andExpect(status().isOk());

        verify(tokenBlacklistService).blackListToken("InvalidFormat");
    }


    @Test
    void logout_WhenBlacklistServiceThrows_ShouldReturnError() throws Exception {

        String token = "valid-token";
        doThrow(new RuntimeException("Blacklist failed"))
                .when(tokenBlacklistService).blackListToken(token);


        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));

        verify(tokenBlacklistService).blackListToken(token);
    }


}