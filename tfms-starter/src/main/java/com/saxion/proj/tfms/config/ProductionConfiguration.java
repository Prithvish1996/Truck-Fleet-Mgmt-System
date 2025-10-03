package com.saxion.proj.tfms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Production configuration properties.
 * Controls whether the application runs in production mode with integrated frontend.
 */
@Configuration
@ConfigurationProperties(prefix = "production")
public class ProductionConfiguration {
    
    private boolean isEnable = false;
    
    public boolean isEnable() {
        return isEnable;
    }
    
    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }
    
    public String getMode() {
        return isEnable ? "Production" : "Development";
    }
}
