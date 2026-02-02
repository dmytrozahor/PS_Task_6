package com.dmytrozah.profitsoft.service.exception;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorNameDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAuthorNameException extends RuntimeException {

    public InvalidAuthorNameException(AuthorNameDto dto) {
        super("Author name %s is invalid.".formatted(dto.firstName() + " " + dto.lastName()));
    }
}
