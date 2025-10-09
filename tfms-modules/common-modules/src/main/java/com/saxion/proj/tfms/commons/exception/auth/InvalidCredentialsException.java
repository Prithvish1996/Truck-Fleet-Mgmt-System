package com.saxion.proj.tfms.commons.exception.auth;

/**
 * Exception thrown when user credentials are invalid
 */
public class InvalidCredentialsException extends AuthenticationException {
    
    public InvalidCredentialsException() {
        super("Invalid username or password", "INVALID_CREDENTIALS");
    }
    
    public InvalidCredentialsException(String message) {
        super(message, "INVALID_CREDENTIALS");
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, "INVALID_CREDENTIALS", cause);
    }
}
