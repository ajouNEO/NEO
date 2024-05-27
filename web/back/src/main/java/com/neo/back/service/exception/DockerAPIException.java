package com.neo.back.service.exception;

public class DockerAPIException extends Exception {
    public DockerAPIException(String message) {
        super(message);
    }

    public DockerAPIException() {
    }
}
