package com.saxion.proj.tfms.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS configuration for production mode.
 * More restrictive settings for security.
 */
@Configuration
@Profile("prod")
public class ProdCorsConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Production domains only
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "https://yourdomain.com",
            "https://www.yourdomain.com",
            "https://api.yourdomain.com"
        ));
        
        // Essential methods only
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE"
        ));
        
        // Specific headers for security
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type", 
            "Authorization",
            "X-Requested-With"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(86400L); // 24 hours

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}