package com.saxion.proj.tfms.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC configuration for serving React SPA
 * Handles client-side routing by serving index.html for all non-API routes
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources (JS, CSS, images, etc.)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/")
                .setCachePeriod(3600);

        // Serve other static files (favicon, manifest, etc.)
        registry.addResourceHandler("/favicon.ico", "/manifest.json", "/robots.txt", "/logo*.png")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // Handle React Router - serve index.html for all non-API routes
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // If the requested resource exists, serve it
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // For API routes, don't serve index.html (let Spring handle them)
                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/") || 
                            resourcePath.startsWith("swagger-ui") || resourcePath.startsWith("v3/api-docs")) {
                            return null;
                        }
                        
                        // For all other routes (React Router routes), serve index.html
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}

