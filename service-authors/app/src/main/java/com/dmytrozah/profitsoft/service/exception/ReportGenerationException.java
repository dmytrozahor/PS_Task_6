package com.dmytrozah.profitsoft.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportGenerationException extends RuntimeException {

    public ReportGenerationException(Exception e) {
        super("There was an error during report generation", e);
    }

}
