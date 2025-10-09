# TFMS Configuration Package Structure

This document describes the organized configuration package structure for the TFMS application.

## Package Organization

### `com.saxion.proj.tfms.config.security`
Security-related configurations and filters:
- `SecurityConfig.java` - Main Spring Security configuration with JWT authentication
- `JwtAuthenticationFilter.java` - JWT token validation filter
- `SecurityErrorHandler.java` - Global security error handling with generic responses
- `SecurityHeaderConfig.java` - Security headers configuration (HSTS, Content-Type Options, etc.)
- `RateLimitingFilter.java` - Rate limiting filter to prevent DoS attacks

### `com.saxion.proj.tfms.config.web`
Web and HTTP-related configurations:
- `DevCorsConfiguration.java` - CORS configuration for development environment
- `ProdCorsConfiguration.java` - CORS configuration for production environment

### `com.saxion.proj.tfms.config.data`
Data initialization and database-related configurations:
- `DefaultUserDataInitializer.java` - Default user creation for development/testing

## Benefits of This Structure

1. **Separation of Concerns**: Each package handles a specific aspect of configuration
2. **Maintainability**: Easy to locate and modify specific types of configurations
3. **Scalability**: New configuration types can be added to appropriate packages
4. **Team Development**: Different team members can work on different aspects without conflicts
5. **Testing**: Easier to write focused unit tests for specific configuration areas

## Import Dependencies

- Security package components are referenced by web package when needed
- Data package is independent and handles its own initialization
- All packages follow Spring Boot auto-configuration patterns
