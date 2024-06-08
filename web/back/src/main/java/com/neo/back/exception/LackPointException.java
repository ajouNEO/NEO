package com.neo.back.exception;

public class LackPointException extends SecurityException {
    public LackPointException(String message) {
        super(message);
    }

    public LackPointException() {
    }
}
