package com.neo.back.docker.exception;

public class DoNotHaveServerException extends SecurityException {
    public DoNotHaveServerException(String message) {
        super(message);
    }

    public DoNotHaveServerException() {
    }
}
