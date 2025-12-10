package com.moa.exception;

public class ProfileAlreadyExistException extends RuntimeException {
    public ProfileAlreadyExistException(String message) {
        super(message);
    }
}
