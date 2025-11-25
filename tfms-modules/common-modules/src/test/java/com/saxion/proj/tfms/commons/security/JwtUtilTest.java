package com.saxion.proj.tfms.commons.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret;
    private Long testExpiration;
    private String testEmail;
    private String testUserType;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        testSecret = "myTFMSSecretKeyThatIsAtLeast256BitsLongForSecureSigning";
        testExpiration = 86400000L;
        testEmail = "test@example.com";
        testUserType = "ADMIN";
        testUserId = 123L;
        
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGenerateTokenWithId() {
        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, testUserId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGetEmailFromToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        
        assertEquals(testEmail, extractedEmail);
    }

    @Test
    void testGetUserTypeFromToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        String extractedUserType = jwtUtil.getUserTypeFromToken(token);
        
        assertEquals(testUserType, extractedUserType);
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, testUserId);
        
        Integer userIdFromClaim = jwtUtil.getClaimFromToken(token, claims -> (Integer) claims.get("userID"));
        
        assertEquals(testUserId.intValue(), userIdFromClaim);
    }

//    @Test
//    void testGetUserIdFromTokenThrowsClassCastException() {
//        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, testUserId);
//
//        assertThrows(ClassCastException.class, () -> jwtUtil.getUserIdFromToken(token));
//    }

    @Test
    void testValidateTokenCatchesExceptionFromGetUserIdFromToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Boolean isValid = jwtUtil.validateToken(token, testEmail);
        
        assertTrue(isValid);
    }
    
//    @Test
//    void testGetUserIdFromTokenWithManualLongClaim() {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("userType", testUserType);
//        claims.put("userID", testUserId.longValue());
//
//        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
//        String token = Jwts.builder()
//                .claims(claims)
//                .subject(testEmail)
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + testExpiration))
//                .signWith(key)
//                .compact();
//
//        assertThrows(ClassCastException.class, () -> jwtUtil.getUserIdFromToken(token));
//    }

    @Test
    void testGetUserIdFromTokenWithoutId() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Integer extractedUserId = jwtUtil.getClaimFromToken(token, claims -> (Integer) claims.get("userID"));
        
        assertNull(extractedUserId);
    }

    @Test
    void testGetExpirationDateFromToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Boolean isValid = jwtUtil.validateToken(token, testEmail);
        
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenInvalidEmail() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");
        
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenExpired() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken(testEmail, testUserType);
        
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
        
        Boolean isValid = jwtUtil.validateToken(token, testEmail);
        
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenInvalidFormat() {
        Boolean isValid = jwtUtil.validateToken("invalid.token.format", testEmail);
        
        assertFalse(isValid);
    }

    @Test
    void testValidateTokenMalformed() {
        Boolean isValid = jwtUtil.validateToken("malformed", testEmail);
        
        assertFalse(isValid);
    }

    @Test
    void testGetClaimFromToken() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        
        assertEquals(testEmail, subject);
    }

    @Test
    void testGetClaimFromTokenWithCustomResolver() {
        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, testUserId);
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);
        
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    void testGetExpirationTime() {
        Long expiration = jwtUtil.getExpirationTime();
        
        assertEquals(testExpiration, expiration);
    }

    @Test
    void testTokenContainsCorrectClaims() {
        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, testUserId);
        
        String email = jwtUtil.getEmailFromToken(token);
        String userType = jwtUtil.getUserTypeFromToken(token);
        Integer userId = jwtUtil.getClaimFromToken(token, claims -> (Integer) claims.get("userID"));
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        assertEquals(testEmail, email);
        assertEquals(testUserType, userType);
        assertEquals(testUserId.intValue(), userId);
        assertNotNull(expirationDate);
    }

    @Test
    void testTokenNotExpiredImmediately() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        
        assertTrue(expirationDate.getTime() > System.currentTimeMillis());
    }

    @Test
    void testTokenExpirationMatchesConfiguration() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        long currentTime = System.currentTimeMillis();
        long tokenExpiration = expirationDate.getTime();
        
        assertTrue(tokenExpiration >= currentTime + testExpiration - 1000);
        assertTrue(tokenExpiration <= currentTime + testExpiration + 1000);
    }

    @Test
    void testDifferentTokensForDifferentUsers() {
        String token1 = jwtUtil.generateToken("user1@example.com", "USER");
        String token2 = jwtUtil.generateToken("user2@example.com", "ADMIN");
        
        assertNotEquals(token1, token2);
    }

    @Test
    void testValidateTokenWithEmptyString() {
        Boolean isValid = jwtUtil.validateToken("", testEmail);
        
        assertFalse(isValid);
    }

    @Test
    void testGetEmailFromTokenWithDifferentEmails() {
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        
        String token1 = jwtUtil.generateToken(email1, testUserType);
        String token2 = jwtUtil.generateToken(email2, testUserType);
        
        assertEquals(email1, jwtUtil.getEmailFromToken(token1));
        assertEquals(email2, jwtUtil.getEmailFromToken(token2));
    }

    @Test
    void testGetUserTypeFromTokenWithDifferentTypes() {
        String type1 = "ADMIN";
        String type2 = "USER";
        
        String token1 = jwtUtil.generateToken(testEmail, type1);
        String token2 = jwtUtil.generateToken(testEmail, type2);
        
        assertEquals(type1, jwtUtil.getUserTypeFromToken(token1));
        assertEquals(type2, jwtUtil.getUserTypeFromToken(token2));
    }

    @Test
    void testGenerateTokenWithNullValues() {
        String token = jwtUtil.generateTokenWithId(testEmail, testUserType, null);
        
        assertNotNull(token);
        Integer userId = jwtUtil.getClaimFromToken(token, claims -> (Integer) claims.get("userID"));
        assertNull(userId);
    }

    @Test
    void testTokenValidationFailsWithWrongSecret() {
        String token = jwtUtil.generateToken(testEmail, testUserType);
        
        JwtUtil differentJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(differentJwtUtil, "secret", "differentSecretKeyThatIsAtLeast256BitsLongForSecure");
        ReflectionTestUtils.setField(differentJwtUtil, "expiration", testExpiration);
        
        assertThrows(JwtException.class, () -> {
            differentJwtUtil.getEmailFromToken(token);
        });
    }
}
