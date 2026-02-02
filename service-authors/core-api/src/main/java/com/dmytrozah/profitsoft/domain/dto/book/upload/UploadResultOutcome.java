package com.dmytrozah.profitsoft.domain.dto.book.upload;

public enum UploadResultOutcome {

    SUCCESS("Book uploaded successfully"),

    AUTHOR_NOT_FOUND("Requested author doesn't exist"),

    TITLE_AUTHOR_EXISTS("Book with the requested title and author already exists");

    private String message;

    UploadResultOutcome(String message) {
        this.message = message;
    }
}
