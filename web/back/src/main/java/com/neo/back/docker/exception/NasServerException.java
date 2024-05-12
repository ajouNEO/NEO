package com.neo.back.docker.exception;

public class NasServerException extends SecurityException {
    public NasServerException(String message) {
        super(message);
    }

    public NasServerException() {
    }
}
