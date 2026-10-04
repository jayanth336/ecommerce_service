package com.org.ecommerce.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token");
    }
}
