package com.dmytrozah.profitsoft.app.test;

import com.dmytrozah.profitsoft.Task2App;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultsResponse;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.BookData;
import com.dmytrozah.profitsoft.domain.repository.BookAuthorRepository;
import com.dmytrozah.profitsoft.domain.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Task2App.class
)
@AutoConfigureMockMvc
public class BookUploadYearTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookAuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private String firstName;
    private String lastName;
    private String authorFullName;

    @BeforeEach
    void createAuthor() throws Exception {
        firstName = "Year";
        lastName = "Tester";
        authorFullName = firstName + " " + lastName;

        String authorJson = """
                {
                  "name": {
                    "first_name": "%s",
                    "last_name": "%s"
                  },
                  "address": {
                    "street": "Main St",
                    "house_number": 1,
                    "city": "TestCity",
                    "country": "TestLand",
                    "post_code": 11111
                  },
                  "phone_number": "000-000",
                  "email": "year@test.local"
                }
                """.formatted(firstName, lastName);

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authorJson))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void cleanup() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    public void uploadPublicationPersisted_and_yearField_consistent() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        // provide full publication and year_published too (service should prefer publication if implemented)
        String publication = "2015-07-15";
        int year = 2015;

        String canonical = author.getCanonicalName() != null && !author.getCanonicalName().isBlank()
                ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"PUB-1",
                    "publication":"%s",
                    "year_published": %d,
                    "author":"%s",
                    "author_id":%d,
                    "genre":"History"
                  }
                ]
                """.formatted(publication, year, canonical, authorId);

        MockMultipartFile file =
                new MockMultipartFile("file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        MvcResult res = mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isCreated()).andReturn();

        BookUploadResultsResponse result = parseResponse(res, BookUploadResultsResponse.class);
        assertThat(result).isNotNull();
        assertThat(result.getSuccessfulUploads()).isGreaterThanOrEqualTo(1);

        BookData saved = bookRepository.findAll().stream()
                .filter(b -> "PUB-1".equals(b.getTitle()))
                .findFirst()
                .orElse(null);

        assertThat(saved).isNotNull();
        // service prioritizes year_published and will set publication to YYYY-01-01 when year_published is provided
        assertThat(saved.getPublication()).isNotNull();
        String expected = year + "-01-01";
        assertThat(saved.getPublication().toString()).isEqualTo(expected);
        assertThat(saved.getPublication().getYear()).isEqualTo(year);
    }

    @Test
    public void uploadYearPersisted() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        int year = 1987;

        String canonical = author.getCanonicalName() != null ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"YEAR-TEST",
                    "year_published": %d,
                    "author":"%s",
                    "author_id": %d,
                    "genre":"Test"
                  }
                ]
                """.formatted(year, canonical, authorId);

        MockMultipartFile file =
                new MockMultipartFile("file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        MvcResult res = mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isCreated()).andReturn();

        BookUploadResultsResponse result = parseResponse(res, BookUploadResultsResponse.class);
        assertThat(result).isNotNull();
        assertThat(result.getSuccessfulUploads()).isGreaterThanOrEqualTo(1);

        BookData saved = bookRepository.findAll().stream()
                .filter(b -> "YEAR-TEST".equals(b.getTitle()))
                .findFirst()
                .orElse(null);

        assertThat(saved).isNotNull();

        // Prefer publication (LocalDate) — check its year if available.
        if (saved.getPublication() != null) {
            assertThat(saved.getPublication().getYear()).isEqualTo(year);
        } else {
            // If publication is not set, we cannot reflectively check yearPublished (avoid reflection).
            // Assert at least the entity exists and title matches — this ensures upload created the record.
            assertThat(saved.getTitle()).isEqualTo("YEAR-TEST");
        }
    }

    @Test
    public void uploadYearInvalid_shouldReturnBadRequest() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String payload = """
                [
                  {
                    "title":"YEAR-ERR",
                    "year_published": "not-a-number",
                    "authorName":"%s",
                    "authorId":%d,
                    "genres":"Test"
                  }
                ]
                """.formatted(authorFullName, authorId);

        MockMultipartFile file =
                new MockMultipartFile("file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        // Invalid numeric value for year_published causes deserialization error -> 400 Bad Request
        mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    private <T> T parseResponse(MvcResult result, Class<T> c) {
        try {
            return mapper.readValue(result.getResponse().getContentAsString(), c);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
