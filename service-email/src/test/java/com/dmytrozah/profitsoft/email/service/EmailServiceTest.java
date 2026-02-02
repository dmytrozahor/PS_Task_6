package com.dmytrozah.profitsoft.email.service;

import com.dmytrozah.profitsoft.email.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailMessageRepository repository;

    @InjectMocks
    private EmailService emailService;

    private EmailRequestDto emailRequest;
    private EmailMessage emailMessage;

    @BeforeEach
    void setUp() {
        emailRequest = new EmailRequestDto(
                "Test Subject",
                "Test Content",
                List.of("test@example.com")
        );

        emailMessage = EmailMessage.builder()
                .id("test-id")
                .recipients(List.of("test@example.com"))
                .subject("Test Subject")
                .content("Test Content")
                .status(EmailStatus.PENDING)
                .retryAttempt(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void processEmailRequest_shouldSaveEmailAndTriggerAsyncSend() {
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);
        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.processEmailRequest(emailRequest);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository, atLeastOnce()).save(captor.capture());

        EmailMessage savedMessage = captor.getAllValues().get(0);
        assertThat(savedMessage.getRecipients()).isEqualTo(emailRequest.recipients());
        assertThat(savedMessage.getSubject()).isEqualTo(emailRequest.subject());
        assertThat(savedMessage.getContent()).isEqualTo(emailRequest.content());
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.PENDING);
        assertThat(savedMessage.getRetryAttempt()).isZero();
        assertThat(savedMessage.getCreatedAt()).isNotNull();
    }
    @Test
    void sendEmailAsync_shouldSendEmailSuccessfully() {
        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        emailService.sendEmailAsync("test-id");

        verify(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository).save(captor.capture());

        EmailMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(savedMessage.getLastAttemptAt()).isNotNull();
    }

    @Test
    void sendEmailAsync_shouldThrowException_whenMessageNotFound() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailService.sendEmailAsync("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email message not found");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailAsync_shouldSkipSending_whenEmailAlreadySent() {
        emailMessage.setStatus(EmailStatus.SENT);
        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));

        emailService.sendEmailAsync("test-id");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(repository, never()).save(any(EmailMessage.class));
    }

    @Test
    void sendEmailAsync_shouldHandleFailure_whenSendingFails() {
        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        emailService.sendEmailAsync("test-id");

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository).save(captor.capture());

        EmailMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(savedMessage.getErrorMessage()).contains("MailSendException");
        assertThat(savedMessage.getErrorMessage()).contains("SMTP error");
        assertThat(savedMessage.getLastAttemptAt()).isNotNull();
        assertThat(savedMessage.getRetryAttempt()).isZero(); // First attempt doesn't increment
    }

    @Test
    void retryEmail_shouldSendEmailSuccessfully() {
        emailMessage.setStatus(EmailStatus.FAILED);
        emailMessage.setRetryAttempt(1);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        emailService.retryEmail(emailMessage);

        verify(mailSender).send(any(SimpleMailMessage.class));
        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository).save(captor.capture());

        EmailMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(savedMessage.getLastAttemptAt()).isNotNull();
    }

    @Test
    void retryEmail_shouldSkipRetry_whenEmailAlreadySent() {
        emailMessage.setStatus(EmailStatus.SENT);

        emailService.retryEmail(emailMessage);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(repository, never()).save(any(EmailMessage.class));
    }

    @Test
    void retryEmail_shouldIncrementRetryAttempt_whenFails() {
        emailMessage.setStatus(EmailStatus.FAILED);
        emailMessage.setRetryAttempt(1);
        doThrow(new MailSendException("Connection timeout")).when(mailSender).send(any(SimpleMailMessage.class));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        emailService.retryEmail(emailMessage);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository).save(captor.capture());

        EmailMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(savedMessage.getErrorMessage()).contains("MailSendException");
        assertThat(savedMessage.getErrorMessage()).contains("Connection timeout");
        assertThat(savedMessage.getRetryAttempt()).isEqualTo(2); // Incremented from 1 to 2
        assertThat(savedMessage.getLastAttemptAt()).isNotNull();
    }

    @Test
    void sendEmail_shouldBuildCorrectMailMessage() {
        emailMessage.setRecipients(List.of("user1@example.com", "user2@example.com"));
        emailMessage.setSubject("Important Message");
        emailMessage.setContent("This is the email body");

        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendEmailAsync("test-id");

        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage sentMessage = mailCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly("user1@example.com", "user2@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Important Message");
        assertThat(sentMessage.getText()).isEqualTo("This is the email body");
    }

    @Test
    void retryEmail_shouldHandleMultipleRetries() {
        emailMessage.setStatus(EmailStatus.FAILED);
        emailMessage.setRetryAttempt(3);
        doThrow(new MailSendException("Still failing")).when(mailSender).send(any(SimpleMailMessage.class));
        when(repository.save(any(EmailMessage.class))).thenReturn(emailMessage);

        emailService.retryEmail(emailMessage);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(repository).save(captor.capture());

        EmailMessage savedMessage = captor.getValue();
        assertThat(savedMessage.getRetryAttempt()).isEqualTo(4);
        assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.FAILED);
    }
}