package com.neo.back.exception;

public class DoNotHaveServerException extends SecurityException {
    public DoNotHaveServerException(String message) {
        super(message);
    }

    public DoNotHaveServerException() {
    }
}
