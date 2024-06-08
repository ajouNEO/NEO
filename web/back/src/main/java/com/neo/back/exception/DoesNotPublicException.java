package com.neo.back.exception;

public class DoesNotPublicException extends SecurityException {
    public DoesNotPublicException(String message) {
        super(message);
    }

    public DoesNotPublicException() {
    }
}
