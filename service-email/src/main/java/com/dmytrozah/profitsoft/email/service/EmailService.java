package com.dmytrozah.profitsoft.email.service;

import com.dmytrozah.profitsoft.email.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailMessageRepository repository;

    public void processEmailRequest(EmailRequestDto request) {
        EmailMessage message = EmailMessage.builder()
                .recipients(request.recipients())
                .subject(request.subject())
                .content(request.content())
                .status(EmailStatus.PENDING)
                .retryAttempt(0)
                .createdAt(LocalDateTime.now())
                .build();

        message = repository.save(message);
        sendEmailAsync(message.getId());
    }

    @Async
    public void sendEmailAsync(String messageId) {
        EmailMessage message = repository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Email message not found: " + messageId));

        if (message.getStatus() == EmailStatus.SENT) {
            log.info("Email already sent, skipping: {}", messageId);
            return;
        }

        sendEmail(message);
    }

    public void retryEmail(EmailMessage message) {
        if (message.getStatus() == EmailStatus.SENT) {
            log.info("Email already sent, skipping retry: {}", message.getId());
            return;
        }

        try {
            send(buildMailMessage(message));
            markAsSent(message);
        } catch (Exception e) {
            handleFailure(message, e, true);
        }
    }

    private void sendEmail(EmailMessage message) {
        try {
            send(buildMailMessage(message));
            markAsSent(message);
        } catch (Exception e) {
            handleFailure(message, e, false);
        }
    }

    private void send(SimpleMailMessage mailMessage) {
        mailSender.send(mailMessage);
    }

    private SimpleMailMessage buildMailMessage(EmailMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.getRecipients().toArray(new String[0]));
        mailMessage.setSubject(message.getSubject());
        mailMessage.setText(message.getContent());
        return mailMessage;
    }

    private void markAsSent(EmailMessage message) {
        message.setStatus(EmailStatus.SENT);
        message.setLastAttemptAt(LocalDateTime.now());
        repository.save(message);
    }

    private void handleFailure(
            EmailMessage message,
            Exception e,
            boolean incrementRetry
    ) {
        message.setStatus(EmailStatus.FAILED);
        message.setErrorMessage(e.getClass().getName() + ": " + e.getMessage());
        message.setLastAttemptAt(LocalDateTime.now());

        if (incrementRetry) {
            message.setRetryAttempt(message.getRetryAttempt() + 1);
        }

        repository.save(message);
    }
}