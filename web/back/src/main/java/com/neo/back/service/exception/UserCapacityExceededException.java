package com.neo.back.service.exception;

public class UserCapacityExceededException extends Exception {
    public UserCapacityExceededException(String message) {
        super(message);
    }

    public UserCapacityExceededException() {

    }
}
