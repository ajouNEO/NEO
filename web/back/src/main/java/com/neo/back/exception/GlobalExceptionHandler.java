package com.neo.back.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DoNotHaveServerException.class)
    public ResponseEntity<String> handleDoNotHaveServerException(DoNotHaveServerException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This user does not have an open server");
    }

    @ExceptionHandler(DualServerException.class)
    public ResponseEntity<String> handleDualServerException(DualServerException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("This user already has an open server");
    }

    @ExceptionHandler(UserCapacityExceededException.class)
    public ResponseEntity<String> handleUserCapacityExceededException(UserCapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("No servers are available");
    }

    @ExceptionHandler(LackPointException.class)
    public ResponseEntity<String> handleLackPointException(LackPointException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("User Point isn't enough for create");
    }

    @ExceptionHandler(NotOwnerException.class)
    public ResponseEntity<String> handleNotOwnerException(NotOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This container is not owned by this user");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
    }

    @ExceptionHandler(NasServerException.class)
    public ResponseEntity<String> handleNasServerException(NasServerException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("NAS error");
    }

    @ExceptionHandler(DoesNotExistGameException.class)
    public ResponseEntity<String> handleDoesNotExistGameException(DoesNotExistGameException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("It's a game that doesn't exist");
    }

    @ExceptionHandler(DoesNotPublicException.class)
    public ResponseEntity<String> handleDoesNotPublicException(DoesNotPublicException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Server that has not been disclosed or does not exist");
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> handleWebClientResponseException(WebClientResponseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error");
    }

    @ExceptionHandler(DockerAPIException.class)
    public ResponseEntity<String> handleDockerAPIException(DockerAPIException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("dockerAPI error");
    }

    @ExceptionHandler(DoesNotExistImageException.class)
    public ResponseEntity<String> handleDoesNotExistImageException(DoesNotExistImageException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This image does not exist in storage");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unknown error : " + ex);
    }
}