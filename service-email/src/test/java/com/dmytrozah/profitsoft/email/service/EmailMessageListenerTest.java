package com.dmytrozah.profitsoft.email.service;

import com.dmytrozah.profitsoft.email.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.email.messaging.EmailMessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailMessageListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailMessageListener listener;

    @Test
    void handleEmailRequest_shouldDelegateToEmailService() {
        EmailRequestDto request = new EmailRequestDto(
                "Test Subject",
                "Test Content",
                List.of("test@example.com")
        );

        listener.handleEmailRequest(request);

        verify(emailService).processEmailRequest(request);
    }

    @Test
    void handleEmailRequest_shouldHandleMultipleRecipients() {
        EmailRequestDto request = new EmailRequestDto(
                "Broadcast Message",
                "Important announcement",
                List.of("user1@example.com", "user2@example.com", "user3@example.com")
        );

        listener.handleEmailRequest(request);

        verify(emailService).processEmailRequest(request);
    }

    @Test
    void handleEmailRequest_shouldHandleEmptyContent() {
        EmailRequestDto request = new EmailRequestDto(
                "Subject Only",
                "",
                List.of("test@example.com")
        );

        listener.handleEmailRequest(request);

        verify(emailService).processEmailRequest(request);
    }
}
