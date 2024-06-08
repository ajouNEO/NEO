package com.neo.back.exception;

public class DoesNotExistImageException extends Exception {
    public DoesNotExistImageException(String message) {
        super(message);
    }

    public DoesNotExistImageException() {
    }
}
