package com.dmytrozah.profitsoft.email.scheduler;

import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import com.dmytrozah.profitsoft.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class EmailRetryScheduler {
    private final EmailMessageRepository repository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void retryFailedEmails() {
        List<EmailMessage> failedMessages = repository.findByStatus(EmailStatus.FAILED);

        for (EmailMessage message : failedMessages) {
            emailService.retryEmail(message);
        }
    }
}

