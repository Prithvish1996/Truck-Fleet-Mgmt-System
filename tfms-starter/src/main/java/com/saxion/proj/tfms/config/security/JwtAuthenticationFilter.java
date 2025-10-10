package com.saxion.proj.tfms.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saxion.proj.tfms.commons.security.JwtUtil;
import com.saxion.proj.tfms.commons.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter to validate JWT tokens and set authentication context
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                  @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            // Blacklist check
            if (tokenBlacklistService.isTokenBlacklisted(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is blacklisted");
                return;
            }
            try {
                email = jwtUtil.getEmailFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn("Unable to get JWT Token: " + e.getMessage());
            }
        }
        
        // Once we get the token validate it.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validate token
            if (jwtUtil.validateToken(jwtToken, email)) {
                
                // Get user type from token
                String userType = jwtUtil.getUserTypeFromToken(jwtToken);
                
                // Create authorities based on user type
                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + userType)
                );
                
                // Create authentication token
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                    
                usernamePasswordAuthenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Set the authentication in the context
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                
            } else {
            }
        }
        
        chain.doFilter(request, response);
    }
}
