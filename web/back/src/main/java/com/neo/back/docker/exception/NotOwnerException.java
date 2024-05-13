package com.neo.back.docker.exception;


public class NotOwnerException extends SecurityException {
    public NotOwnerException(String message) {
        super(message);
    }

    public NotOwnerException() {
    }
}
    



