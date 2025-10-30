package com.saxion.proj.tfms.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Security configuration for TFMS application
 * Disables default login form and configures REST API security with JWT authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(csrf -> csrf.disable())
            
            // Disable default login form
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication popup
            .httpBasic(basic -> basic.disable())
            
            // Disable logout form
            .logout(logout -> logout.disable())
            
            // Configure session management - stateless for REST API
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (no authentication required)
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/auth/login",  // Login endpoint must be public
                    "/api/routing/**"   // Routing matrix endpoints (for testing)
                ).permitAll()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Static resources (if serving frontend)
                .requestMatchers("/static/**", "/", "/index.html").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .referrerPolicy(referrerPolicy -> 
                    referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            );

        return http.build();
    }
}
