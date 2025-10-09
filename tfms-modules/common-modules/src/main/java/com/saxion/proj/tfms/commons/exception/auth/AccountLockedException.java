package com.saxion.proj.tfms.commons.exception.auth;

/**
 * Exception thrown when user account is locked due to multiple failed attempts
 */
public class AccountLockedException extends AuthenticationException {
    
    public AccountLockedException() {
        super("Account is locked due to multiple failed login attempts", "ACCOUNT_LOCKED");
    }
    
    public AccountLockedException(String message) {
        super(message, "ACCOUNT_LOCKED");
    }
    
    public AccountLockedException(String message, Throwable cause) {
        super(message, "ACCOUNT_LOCKED", cause);
    }
}
