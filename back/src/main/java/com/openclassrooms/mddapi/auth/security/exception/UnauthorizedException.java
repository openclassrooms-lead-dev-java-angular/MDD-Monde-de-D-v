package com.openclassrooms.mddapi.auth.security.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {

        super(message);
    }
}
