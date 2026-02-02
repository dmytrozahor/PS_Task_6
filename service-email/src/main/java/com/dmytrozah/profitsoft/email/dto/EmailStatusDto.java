package com.dmytrozah.profitsoft.email.dto;

import com.dmytrozah.profitsoft.email.model.EmailStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EmailStatusDto(
        String id,
        String subject,
        List<String> recipients,
        EmailStatus status,
        String errorMessage,
        Integer retryAttempt,
        LocalDateTime createdAt,
        LocalDateTime lastAttemptAt
) {
}