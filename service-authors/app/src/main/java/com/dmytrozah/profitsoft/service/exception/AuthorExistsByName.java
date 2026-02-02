package com.dmytrozah.profitsoft.service.exception;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;
import jakarta.persistence.EntityExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AuthorExistsByName extends EntityExistsException {

    public AuthorExistsByName(final AuthorSaveDto dto) {
        super("Author with name %s already exists.".formatted(
                dto.getName().firstName() + " " + dto.getName().lastName())
        );
    }
}