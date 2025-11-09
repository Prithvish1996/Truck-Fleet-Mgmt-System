// /tfms-commons/src/main/java/com/saxion/proj/tfms/commons/security/JwtUtil.java
package com.saxion.proj.tfms.commons.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;



@Component
public class JwtUtil {


    @Value("${jwt.secret:myTFMSSecretKeyThatIsAtLeast256BitsLongForSecureSigning}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private Long expiration;


    
    // Generate token with userType as role
    public String generateToken(String email, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        return createToken(claims, email);
    }

    // Generate token with email, userType as role, userId
    public String generateTokenWithId(String email, String userType, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("userID", userId);
        return createToken(claims, email);
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
    
    // Validate token
    public Boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = getEmailFromToken(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    // Extract email from token
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    // Extract userType from token
    public String getUserTypeFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("userType"));
    }

    // Extract userId from token
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> {
            Object userId = claims.get("userID");
            if (userId == null) {
                return null;
            }
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
            throw new IllegalArgumentException("userID claim is not a number: " + userId.getClass());
        });
    }
    
    // Get expiration date
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    // Generic method to extract claims
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    // Parse all claims from token
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // Get remaining time in milliseconds
    public Long getExpirationTime() {
        return expiration;
    }
}