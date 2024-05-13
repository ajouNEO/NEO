package com.neo.back.docker.exception;

public class UserCapacityExceededException extends Exception {
    public UserCapacityExceededException(String message) {
        super(message);
    }

    public UserCapacityExceededException() {

    }
}
