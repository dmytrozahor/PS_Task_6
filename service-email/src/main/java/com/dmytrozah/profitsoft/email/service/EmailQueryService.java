package com.dmytrozah.profitsoft.email.service;

import com.dmytrozah.profitsoft.email.dto.EmailStatusDto;
import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailQueryService {
    private final EmailMessageRepository repository;

    public Page<EmailStatusDto> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toDto);
    }

    public Page<EmailStatusDto> findByStatus(EmailStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(this::toDto);
    }

    public EmailStatusDto findById(String id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
    }

    private EmailStatusDto toDto(EmailMessage message) {
        return new EmailStatusDto(
                message.getId(),
                message.getSubject(),
                message.getRecipients(),
                message.getStatus(),
                message.getErrorMessage(),
                message.getRetryAttempt(),
                message.getCreatedAt(),
                message.getLastAttemptAt()
        );
    }
}