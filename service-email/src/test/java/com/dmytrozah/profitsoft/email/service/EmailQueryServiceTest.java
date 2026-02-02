package com.dmytrozah.profitsoft.email.service;

import com.dmytrozah.profitsoft.email.dto.EmailStatusDto;
import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailQueryServiceTest {

    @Mock
    private EmailMessageRepository repository;

    @InjectMocks
    private EmailQueryService queryService;

    private EmailMessage emailMessage;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        emailMessage = EmailMessage.builder()
                .id("test-id")
                .subject("Test Subject")
                .recipients(List.of("test@example.com"))
                .content("Test Content")
                .status(EmailStatus.SENT)
                .retryAttempt(0)
                .createdAt(LocalDateTime.of(2026, 1, 18, 10, 0))
                .lastAttemptAt(LocalDateTime.of(2026, 1, 18, 10, 5))
                .build();
    }

    @Test
    void findAll_shouldReturnPageOfEmailStatusDtos() {
        Page<EmailMessage> messagePage = new PageImpl<>(List.of(emailMessage));
        when(repository.findAll(pageable)).thenReturn(messagePage);

        Page<EmailStatusDto> result = queryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        EmailStatusDto dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo("test-id");
        assertThat(dto.subject()).isEqualTo("Test Subject");
        assertThat(dto.recipients()).containsExactly("test@example.com");
        assertThat(dto.status()).isEqualTo(EmailStatus.SENT);
        assertThat(dto.retryAttempt()).isZero();
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoMessages() {
        Page<EmailMessage> emptyPage = new PageImpl<>(List.of());
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<EmailStatusDto> result = queryService.findAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findByStatus_shouldReturnFilteredMessages() {
        Page<EmailMessage> messagePage = new PageImpl<>(List.of(emailMessage));
        when(repository.findByStatus(EmailStatus.SENT, pageable)).thenReturn(messagePage);

        Page<EmailStatusDto> result = queryService.findByStatus(EmailStatus.SENT, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(EmailStatus.SENT);
    }

    @Test
    void findByStatus_shouldReturnOnlyFailedMessages() {
        EmailMessage failedMessage = EmailMessage.builder()
                .id("failed-id")
                .subject("Failed Email")
                .recipients(List.of("fail@example.com"))
                .status(EmailStatus.FAILED)
                .errorMessage("SMTP error")
                .retryAttempt(3)
                .createdAt(LocalDateTime.now())
                .build();

        Page<EmailMessage> messagePage = new PageImpl<>(List.of(failedMessage));
        when(repository.findByStatus(EmailStatus.FAILED, pageable)).thenReturn(messagePage);

        Page<EmailStatusDto> result = queryService.findByStatus(EmailStatus.FAILED, pageable);

        assertThat(result.getContent()).hasSize(1);
        EmailStatusDto dto = result.getContent().get(0);
        assertThat(dto.status()).isEqualTo(EmailStatus.FAILED);
        assertThat(dto.errorMessage()).isEqualTo("SMTP error");
        assertThat(dto.retryAttempt()).isEqualTo(3);
    }

    @Test
    void findById_shouldReturnEmailStatusDto() {
        when(repository.findById("test-id")).thenReturn(Optional.of(emailMessage));

        EmailStatusDto result = queryService.findById("test-id");

        assertThat(result.id()).isEqualTo("test-id");
        assertThat(result.subject()).isEqualTo("Test Subject");
        assertThat(result.recipients()).containsExactly("test@example.com");
        assertThat(result.status()).isEqualTo(EmailStatus.SENT);
        assertThat(result.errorMessage()).isNull();
        assertThat(result.retryAttempt()).isZero();
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 18, 10, 0));
        assertThat(result.lastAttemptAt()).isEqualTo(LocalDateTime.of(2026, 1, 18, 10, 5));
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryService.findById("non-existent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email not found: non-existent");
    }

    @Test
    void findAll_shouldMapAllFieldsCorrectly() {
        EmailMessage detailedMessage = EmailMessage.builder()
                .id("detailed-id")
                .subject("Detailed Subject")
                .recipients(List.of("user1@example.com", "user2@example.com"))
                .status(EmailStatus.PENDING)
                .errorMessage(null)
                .retryAttempt(0)
                .createdAt(LocalDateTime.of(2026, 1, 18, 12, 0))
                .lastAttemptAt(null)
                .build();

        Page<EmailMessage> messagePage = new PageImpl<>(List.of(detailedMessage));
        when(repository.findAll(pageable)).thenReturn(messagePage);

        Page<EmailStatusDto> result = queryService.findAll(pageable);

        EmailStatusDto dto = result.getContent().get(0);
        assertThat(dto.id()).isEqualTo("detailed-id");
        assertThat(dto.subject()).isEqualTo("Detailed Subject");
        assertThat(dto.recipients()).containsExactly("user1@example.com", "user2@example.com");
        assertThat(dto.status()).isEqualTo(EmailStatus.PENDING);
        assertThat(dto.errorMessage()).isNull();
        assertThat(dto.retryAttempt()).isZero();
        assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 18, 12, 0));
        assertThat(dto.lastAttemptAt()).isNull();
    }

    @Test
    void findByStatus_shouldHandlePagination() {
        EmailMessage message1 = EmailMessage.builder()
                .id("id-1")
                .subject("Subject 1")
                .recipients(List.of("test1@example.com"))
                .status(EmailStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        EmailMessage message2 = EmailMessage.builder()
                .id("id-2")
                .subject("Subject 2")
                .recipients(List.of("test2@example.com"))
                .status(EmailStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        Page<EmailMessage> messagePage = new PageImpl<>(
                List.of(message1, message2),
                pageable,
                2
        );
        when(repository.findByStatus(EmailStatus.SENT, pageable)).thenReturn(messagePage);

        Page<EmailStatusDto> result = queryService.findByStatus(EmailStatus.SENT, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.getContent().get(0).id()).isEqualTo("id-1");
        assertThat(result.getContent().get(1).id()).isEqualTo("id-2");
    }
}
