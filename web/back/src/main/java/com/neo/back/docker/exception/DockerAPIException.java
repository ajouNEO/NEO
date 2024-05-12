package com.neo.back.docker.exception;

public class DockerAPIException extends Exception {
    public DockerAPIException(String message) {
        super(message);
    }

    public DockerAPIException() {
    }
}
