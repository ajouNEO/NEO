package com.neo.back.exception;

public class DockerAPIException extends Exception {
    public DockerAPIException(String message) {
        super(message);
    }

    public DockerAPIException() {
    }
}
