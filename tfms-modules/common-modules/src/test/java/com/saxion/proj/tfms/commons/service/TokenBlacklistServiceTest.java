package com.saxion.proj.tfms.commons.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void blackListToken_shouldAddTokenToBlacklist() {
        String token = "test-token-123";
        
        tokenBlacklistService.blackListToken(token);
        
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseForNonBlacklistedToken() {
        String token = "non-blacklisted-token";
        
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);
        
        assertFalse(result);
    }

    @Test
    void isTokenBlacklisted_shouldReturnTrueForBlacklistedToken() {
        String token = "blacklisted-token";
        tokenBlacklistService.blackListToken(token);
        
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);
        
        assertTrue(result);
    }

    @Test
    void isTokenBlacklisted_shouldReturnFalseAndRemoveExpiredToken() {
        String token = "expired-token";
        long twoYearsAgo = System.currentTimeMillis() - (2L * 365 * 24 * 60 * 60 * 1000) - 1000;
        
        Map<String, Long> blacklist = new ConcurrentHashMap<>();
        blacklist.put(token, twoYearsAgo);
        ReflectionTestUtils.setField(tokenBlacklistService, "blacklist", blacklist);
        
        boolean result = tokenBlacklistService.isTokenBlacklisted(token);
        
        assertFalse(result);
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void cleanupExpiredTokens_shouldRemoveExpiredTokensOnly() {
        String validToken = "valid-token";
        String expiredToken = "expired-token";
        
        long currentTime = System.currentTimeMillis();
        long twoYearsAgo = currentTime - (2L * 365 * 24 * 60 * 60 * 1000) - 1000;
        
        Map<String, Long> blacklist = new ConcurrentHashMap<>();
        blacklist.put(validToken, currentTime);
        blacklist.put(expiredToken, twoYearsAgo);
        ReflectionTestUtils.setField(tokenBlacklistService, "blacklist", blacklist);
        
        tokenBlacklistService.cleanupExpiredTokens();
        
        assertTrue(tokenBlacklistService.isTokenBlacklisted(validToken));
        assertFalse(tokenBlacklistService.isTokenBlacklisted(expiredToken));
    }

    @Test
    void cleanupExpiredTokens_shouldNotRemoveRecentlyBlacklistedTokens() {
        String token1 = "token-1";
        String token2 = "token-2";
        
        tokenBlacklistService.blackListToken(token1);
        tokenBlacklistService.blackListToken(token2);
        
        tokenBlacklistService.cleanupExpiredTokens();
        
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
    }

    @Test
    void blackListToken_shouldUpdateExistingTokenTimestamp() {
        String token = "token-to-update";
        
        tokenBlacklistService.blackListToken(token);
        long firstTimestamp = getTokenTimestamp(token);
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        tokenBlacklistService.blackListToken(token);
        long secondTimestamp = getTokenTimestamp(token);
        
        assertTrue(secondTimestamp >= firstTimestamp);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void cleanupExpiredTokens_shouldHandleEmptyBlacklist() {
        tokenBlacklistService.cleanupExpiredTokens();
        
        assertFalse(tokenBlacklistService.isTokenBlacklisted("any-token"));
    }

    @Test
    void isTokenBlacklisted_shouldHandleNullTokenInMap() {
        String token = "test-token";
        
        assertFalse(tokenBlacklistService.isTokenBlacklisted(token));
    }

    @Test
    void cleanupExpiredTokens_shouldRemoveMultipleExpiredTokens() {
        long twoYearsAgo = System.currentTimeMillis() - (2L * 365 * 24 * 60 * 60 * 1000) - 1000;
        
        Map<String, Long> blacklist = new ConcurrentHashMap<>();
        blacklist.put("expired-1", twoYearsAgo);
        blacklist.put("expired-2", twoYearsAgo);
        blacklist.put("expired-3", twoYearsAgo);
        blacklist.put("valid", System.currentTimeMillis());
        ReflectionTestUtils.setField(tokenBlacklistService, "blacklist", blacklist);
        
        tokenBlacklistService.cleanupExpiredTokens();
        
        assertFalse(tokenBlacklistService.isTokenBlacklisted("expired-1"));
        assertFalse(tokenBlacklistService.isTokenBlacklisted("expired-2"));
        assertFalse(tokenBlacklistService.isTokenBlacklisted("expired-3"));
        assertTrue(tokenBlacklistService.isTokenBlacklisted("valid"));
    }

    @SuppressWarnings("unchecked")
    private Long getTokenTimestamp(String token) {
        Map<String, Long> blacklist = (Map<String, Long>) ReflectionTestUtils.getField(tokenBlacklistService, "blacklist");
        if (blacklist == null) {
            return null;
        }
        return blacklist.get(token);
    }
}
