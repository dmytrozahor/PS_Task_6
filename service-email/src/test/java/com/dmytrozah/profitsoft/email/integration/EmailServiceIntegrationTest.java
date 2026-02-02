package com.dmytrozah.profitsoft.email.integration;

import com.dmytrozah.profitsoft.email.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import com.dmytrozah.profitsoft.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class EmailServiceIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.6.1")
    ).withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired
    private EmailMessageRepository repository;

    @MockBean
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        reset(mailSender);
    }

    @Test
    void processEmailRequest_shouldSuccessfullySendEmail() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        EmailRequestDto request = new EmailRequestDto(
                "Integration Test Subject",
                "Integration Test Content",
                List.of("success@example.com")
        );

        emailService.processEmailRequest(request);

        await().untilAsserted(() -> {
            List<EmailMessage> messages = (List<EmailMessage>) repository.findAll();
            assertThat(messages).hasSize(1);

            EmailMessage savedMessage = messages.get(0);
            assertThat(savedMessage.getSubject()).isEqualTo("Integration Test Subject");
            assertThat(savedMessage.getContent()).isEqualTo("Integration Test Content");
            assertThat(savedMessage.getRecipients()).containsExactly("success@example.com");
            assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.SENT);
            assertThat(savedMessage.getErrorMessage()).isNull();
            assertThat(savedMessage.getRetryAttempt()).isZero();
            assertThat(savedMessage.getLastAttemptAt()).isNotNull();
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void processEmailRequest_shouldHandleFailedEmailSending() {
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        EmailRequestDto request = new EmailRequestDto(
                "Failed Email Subject",
                "Failed Email Content",
                List.of("fail@example.com")
        );

        emailService.processEmailRequest(request);

        await().untilAsserted(() -> {
            List<EmailMessage> messages = (List<EmailMessage>) repository.findAll();
            assertThat(messages).hasSize(1);

            EmailMessage savedMessage = messages.get(0);
            assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.FAILED);
            assertThat(savedMessage.getErrorMessage())
                    .contains("MailSendException")
                    .contains("SMTP connection failed");
            assertThat(savedMessage.getRetryAttempt()).isZero();
            assertThat(savedMessage.getLastAttemptAt()).isNotNull();
        });

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void processEmailRequest_shouldHandleMultipleRecipients() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        EmailRequestDto request = new EmailRequestDto(
                "Multiple Recipients",
                "Broadcast message",
                List.of("user1@example.com", "user2@example.com", "user3@example.com")
        );

        emailService.processEmailRequest(request);

        await().untilAsserted(() -> {
            List<EmailMessage> messages = (List<EmailMessage>) repository.findAll();
            assertThat(messages).hasSize(1);

            EmailMessage savedMessage = messages.get(0);
            assertThat(savedMessage.getRecipients()).hasSize(3);
            assertThat(savedMessage.getStatus()).isEqualTo(EmailStatus.SENT);
        });
    }

    @Test
    void retryEmail_shouldSuccessfullyResendFailedEmail() {
        EmailMessage failedMessage = EmailMessage.builder()
                .subject("Retry Test")
                .content("Retry Content")
                .recipients(List.of("retry@example.com"))
                .status(EmailStatus.FAILED)
                .retryAttempt(1)
                .errorMessage("Previous error")
                .build();

        failedMessage = repository.save(failedMessage);

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.retryEmail(failedMessage);

        EmailMessage retriedMessage = repository.findById(failedMessage.getId()).orElseThrow();
        assertThat(retriedMessage.getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(retriedMessage.getLastAttemptAt()).isNotNull();

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void retryEmail_shouldIncrementRetryCountOnFailure() {
        EmailMessage failedMessage = EmailMessage.builder()
                .subject("Retry Fail Test")
                .content("Content")
                .recipients(List.of("fail@example.com"))
                .status(EmailStatus.FAILED)
                .retryAttempt(2)
                .build();

        failedMessage = repository.save(failedMessage);

        doThrow(new MailSendException("Still failing"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        emailService.retryEmail(failedMessage);

        EmailMessage retriedMessage = repository.findById(failedMessage.getId()).orElseThrow();
        assertThat(retriedMessage.getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(retriedMessage.getRetryAttempt()).isEqualTo(3);
        assertThat(retriedMessage.getErrorMessage()).contains("MailSendException");
    }

    @Test
    void sendEmailAsync_shouldSkipAlreadySentEmails() {
        EmailMessage sentMessage = EmailMessage.builder()
                .subject("Already Sent")
                .content("Content")
                .recipients(List.of("sent@example.com"))
                .status(EmailStatus.SENT)
                .retryAttempt(0)
                .build();

        sentMessage = repository.save(sentMessage);

        emailService.sendEmailAsync(sentMessage.getId());

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}