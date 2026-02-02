package com.dmytrozah.profitsoft.email.integration;

import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.repository.EmailMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EmailControllerTest {

    @Container
    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.6.1")
    ).withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void getAllEmails_shouldReturnPageOfEmails() throws Exception {
        EmailMessage message1 = createEmailMessage("Subject 1", EmailStatus.SENT);
        EmailMessage message2 = createEmailMessage("Subject 2", EmailStatus.FAILED);

        repository.saveAll(List.of(message1, message2));
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].subject",
                        containsInAnyOrder("Subject 1", "Subject 2")));
    }

    @Test
    void getEmailsByStatus_shouldReturnFilteredEmails() throws Exception {
        EmailMessage sentMessage = createEmailMessage("Sent Email", EmailStatus.SENT);
        EmailMessage failedMessage = createEmailMessage("Failed Email", EmailStatus.FAILED);
        EmailMessage pendingMessage = createEmailMessage("Pending Email", EmailStatus.PENDING);

        repository.saveAll(List.of(sentMessage, failedMessage, pendingMessage));
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails/status/FAILED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("FAILED")))
                .andExpect(jsonPath("$.content[0].subject", is("Failed Email")));
    }

    @Test
    void getEmailById_shouldReturnSpecificEmail() throws Exception {
        EmailMessage message = createEmailMessage("Test Subject", EmailStatus.SENT);
        message = repository.save(message);
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails/" + message.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(message.getId())))
                .andExpect(jsonPath("$.subject", is("Test Subject")))
                .andExpect(jsonPath("$.status", is("SENT")))
                .andExpect(jsonPath("$.recipients[0]", is("test@example.com")));
    }

    @Test
    void getEmailById_shouldReturn500_whenEmailNotFound() throws Exception {
        mockMvc.perform(get("/api/emails/non-existent-id"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getEmailsByStatus_shouldReturnEmptyPage_whenNoMatchingStatus() throws Exception {
        EmailMessage message = createEmailMessage("Test", EmailStatus.SENT);
        repository.save(message);
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails/status/FAILED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getAllEmails_shouldSupportPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            repository.save(createEmailMessage("Subject " + i, EmailStatus.SENT));
        }
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.totalPages", is(2)));

        mockMvc.perform(get("/api/emails")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)));
    }

    @Test
    void getEmailsByStatus_shouldReturnCompleteEmailDetails() throws Exception {
        EmailMessage message = EmailMessage.builder()
                .subject("Detailed Test")
                .content("Test content")
                .recipients(List.of("user1@example.com", "user2@example.com"))
                .status(EmailStatus.FAILED)
                .errorMessage("Connection timeout")
                .retryAttempt(3)
                .createdAt(LocalDateTime.now())
                .lastAttemptAt(LocalDateTime.now())
                .build();

        repository.save(message);
        Thread.sleep(1000);

        mockMvc.perform(get("/api/emails/status/FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].subject", is("Detailed Test")))
                .andExpect(jsonPath("$.content[0].recipients", hasSize(2)))
                .andExpect(jsonPath("$.content[0].errorMessage", is("Connection timeout")))
                .andExpect(jsonPath("$.content[0].retryAttempt", is(3)))
                .andExpect(jsonPath("$.content[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.content[0].lastAttemptAt", notNullValue()));
    }

    private EmailMessage createEmailMessage(String subject, EmailStatus status) {
        return EmailMessage.builder()
                .subject(subject)
                .content("Test content")
                .recipients(List.of("test@example.com"))
                .status(status)
                .retryAttempt(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
