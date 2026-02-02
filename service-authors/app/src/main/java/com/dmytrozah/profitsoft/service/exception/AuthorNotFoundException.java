package com.dmytrozah.profitsoft.service.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AuthorNotFoundException extends EntityNotFoundException {

    public AuthorNotFoundException(long id) {
        super("Requested author %d not found.".formatted(id));
    }

    public AuthorNotFoundException(String canonicalName) {
        super("Requested author %s not found.".formatted(canonicalName));
    }

}
