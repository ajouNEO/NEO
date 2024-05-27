package com.neo.back.service.exception;


public class NotOwnerException extends SecurityException {
    public NotOwnerException(String message) {
        super(message);
    }

    public NotOwnerException() {
    }
}
    



