package com.openclassrooms.mddapi.user.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException() {
        super("User Already Exists");
    }
}
