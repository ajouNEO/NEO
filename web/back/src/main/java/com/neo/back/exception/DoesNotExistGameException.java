package com.neo.back.exception;

public class DoesNotExistGameException extends Exception {
    public DoesNotExistGameException(String message) {
        super(message);
    }

    public DoesNotExistGameException() {
    }
}