package com.saxion.proj.tfms.config.security;

import com.saxion.proj.tfms.commons.security.JwtUtil;
import com.saxion.proj.tfms.commons.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final String EMAIL = "test@example.com";
    private static final String USER_TYPE = "ADMIN";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
    
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getEmailFromToken(anyString());
        verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    }

    @Test
    void testDoFilterInternal_InvalidAuthorizationHeader_ShouldContinueFilterChain() throws ServletException, IOException {
    
        when(request.getHeader("Authorization")).thenReturn("Invalid Header");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getEmailFromToken(anyString());
        verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    }

    @Test
    void testDoFilterInternal_BlacklistedToken_ShouldReturnUnauthorized() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
        when(response.getWriter()).thenReturn(printWriter);

        
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter).write("Token is blacklisted");
        verify(filterChain, never()).doFilter(request, response);
        verify(jwtUtil, never()).getEmailFromToken(anyString());
    }

    @Test
    void testDoFilterInternal_ExceptionInTokenParsing_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void testDoFilterInternal_ValidTokenButAlreadyAuthenticated_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void testDoFilterInternal_ValidTokenButInvalidValidation_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil).validateToken(VALID_TOKEN, EMAIL);
        verify(jwtUtil, never()).getUserTypeFromToken(anyString());
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    void testDoFilterInternal_ValidTokenAndSuccessfulAuthentication_ShouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil).validateToken(VALID_TOKEN, EMAIL);
        verify(jwtUtil).getUserTypeFromToken(VALID_TOKEN);
        verify(securityContext).setAuthentication(any());
    }

    @Test
    void testDoFilterInternal_NullEmail_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void testDoFilterInternal_EmptyEmail_ShouldValidateToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn("");
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, "")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil).validateToken(VALID_TOKEN, "");
        verify(jwtUtil, never()).getUserTypeFromToken(anyString());
    }

    @Test
    void testDoFilterInternal_BearerTokenWithoutSpace_ShouldContinueFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).getEmailFromToken(anyString());
        verify(tokenBlacklistService, never()).isTokenBlacklisted(anyString());
    }

    @Test
    void testDoFilterInternal_BearerTokenExactlySevenCharacters_ShouldExtractEmptyToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(tokenBlacklistService.isTokenBlacklisted("")).thenReturn(false);
        when(jwtUtil.getEmailFromToken("")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenBlacklistService).isTokenBlacklisted("");
        verify(jwtUtil).getEmailFromToken("");
    }

    @Test
    void testIsABoolean_CallsBlackListCheck() throws IOException {
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        try {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            fail("ServletException should not be thrown");
        }

        verify(tokenBlacklistService).isTokenBlacklisted(VALID_TOKEN);
    }

    @Test
    void testGetEmail_WithValidToken_ShouldReturnEmail() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(jwtUtil).validateToken(VALID_TOKEN, EMAIL);
    }

    @Test
    void testGetEmail_WithException_ShouldReturnNull() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenThrow(new RuntimeException("Token parsing error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        
        verify(jwtUtil).getEmailFromToken(VALID_TOKEN);
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void testBlackListCheck_WithBlacklistedToken_ShouldReturnTrueAndSetResponse() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(true);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter).write("Token is blacklisted");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testBlackListCheck_WithNonBlacklistedToken_ShouldReturnFalse() throws IOException, ServletException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenBlacklistService).isTokenBlacklisted(VALID_TOKEN);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testAuthenticationTokenCreation_ShouldHaveCorrectAuthorities() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(argThat(auth -> {
            if (auth == null) return false;
            
            if (!EMAIL.equals(auth.getPrincipal())) return false;
            
            if (auth.getCredentials() != null) return false;
            
            if (auth.getAuthorities() == null || auth.getAuthorities().size() != 1) return false;
            
            SimpleGrantedAuthority expectedAuthority = new SimpleGrantedAuthority("ROLE_" + USER_TYPE);
            return auth.getAuthorities().contains(expectedAuthority);
        }));
    }

    @Test
    void testDoFilterInternal_AuthenticationDetailsSet() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(securityContext).setAuthentication(argThat(auth -> {
            return auth != null && auth.getDetails() != null;
        }));
    }

    @Test
    void testSecurityContextHolderCleanup() {
        SecurityContextHolder.clearContext();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_TokenWithDifferentUserTypes() throws ServletException, IOException {
        String[] userTypes = {"USER", "ADMIN", "MANAGER", "DRIVER"};
        
        for (String userType : userTypes) {
            reset(securityContext);
            SecurityContextHolder.setContext(securityContext);
            
            when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
            when(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).thenReturn(false);
            when(jwtUtil.getEmailFromToken(VALID_TOKEN)).thenReturn(EMAIL);
            when(securityContext.getAuthentication()).thenReturn(null);
            when(jwtUtil.validateToken(VALID_TOKEN, EMAIL)).thenReturn(true);
            when(jwtUtil.getUserTypeFromToken(VALID_TOKEN)).thenReturn(userType);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(securityContext).setAuthentication(argThat(auth -> {
                SimpleGrantedAuthority expectedAuthority = new SimpleGrantedAuthority("ROLE_" + userType);
                return auth.getAuthorities().contains(expectedAuthority);
            }));
        }
    }

    @Test
    void testDoFilterInternal_LongTokenValue() throws ServletException, IOException {
        String longToken = "a".repeat(1000);
        String longBearerToken = "Bearer " + longToken;
        
        when(request.getHeader("Authorization")).thenReturn(longBearerToken);
        when(tokenBlacklistService.isTokenBlacklisted(longToken)).thenReturn(false);
        when(jwtUtil.getEmailFromToken(longToken)).thenReturn(EMAIL);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.validateToken(longToken, EMAIL)).thenReturn(true);
        when(jwtUtil.getUserTypeFromToken(longToken)).thenReturn(USER_TYPE);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).getEmailFromToken(longToken);
        verify(jwtUtil).validateToken(longToken, EMAIL);
        verify(filterChain).doFilter(request, response);
    }
}
