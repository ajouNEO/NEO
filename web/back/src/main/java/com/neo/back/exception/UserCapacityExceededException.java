package com.neo.back.exception;

public class UserCapacityExceededException extends Exception {
    public UserCapacityExceededException(String message) {
        super(message);
    }

    public UserCapacityExceededException() {

    }
}
