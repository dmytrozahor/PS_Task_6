package com.dmytrozah.profitsoft.rest.controller;

import com.dmytrozah.profitsoft.service.exception.InvalidAuthorNameException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<Object> illegalArgumentException(IllegalArgumentException e){
        log.warn("IllegalArgumentException is thrown: {}", e.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<Object> handleEntityExists(final Exception e) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(final Exception e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidAuthorNameException.class)
    public ResponseEntity<Object> handleInvalidName(final Exception e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MismatchedInputException.class)
    public ResponseEntity<Object> handleMismatchedInputException(final Exception e) {
        log.warn("MismatchedInputException is thrown: {}", e.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "There was an error deserializing the data");
    }

    private static ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message){
        ErrorResponse response = new ErrorResponse(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status.value()).body(response);
    }

    @Getter
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ErrorResponse {
        private int status;
        private String error, message;
    }
}
