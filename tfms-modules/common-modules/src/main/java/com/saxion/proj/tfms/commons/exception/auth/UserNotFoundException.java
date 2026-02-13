package com.saxion.proj.tfms.commons.exception.auth;

/**
 * Exception thrown when user is not found in the system
 */
public class UserNotFoundException extends AuthenticationException {
    
    public UserNotFoundException() {
        super("User not found", "USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String email) {
        super("User not found with email: " + email, "USER_NOT_FOUND");
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, "USER_NOT_FOUND", cause);
    }
}
