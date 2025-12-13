package com.moa.exception;

public class ProfileAlreadyExistException extends RuntimeException {
    public ProfileAlreadyExistException(String message) {
        super("Profile Already Exists");
    }
}
