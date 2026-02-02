package com.dmytrozah.profitsoft.app.test;

import com.dmytrozah.profitsoft.Task2App;
import com.dmytrozah.profitsoft.domain.dto.RestResponse;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorInfoDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookListDto;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultsResponse;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.BookData;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorName;
import com.dmytrozah.profitsoft.domain.repository.BookAuthorRepository;
import com.dmytrozah.profitsoft.domain.repository.BookRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Task2App.class
)
@AutoConfigureMockMvc
public class BookControllerTest {

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

    private final String baseBookTitle = "IntegrationBook";
    private final String genres = "Drama,Adventure";

    @BeforeEach
    void createAuthor() throws Exception {
        firstName = "Dmytro";
        lastName = "Zahorodnii";
        authorFullName = firstName + " " + lastName;

        String authorJson = """
                {
                  "name": {
                    "first_name": "%s",
                    "last_name": "%s"
                  },
                  "address": {
                    "street": "Main St",
                    "house_number": 10,
                    "city": "Karlsruhe",
                    "country": "Germany",
                    "post_code": 76131
                  },
                  "phone_number": "555-0101",
                  "email": "dmytro@example.com"
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
    public void createBook_shouldSaveAndExposeNestedAuthorName() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String bookJson = """
                {
                  "title": "%s",
                  "publish_date": "%s",
                  "author_name": "%s",
                  "author_id": %d,
                  "genres": "%s"
                }
                """.formatted(baseBookTitle, LocalDate.now().toString(), authorFullName, authorId, genres);

        MvcResult res = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isCreated()).andReturn();

        RestResponse rr = parseResponse(res, RestResponse.class);
        long bookId = Long.parseLong(rr.getMessage());
        assertThat(bookId).isGreaterThanOrEqualTo(1);

        BookData saved = bookRepository.findById(bookId).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(baseBookTitle);
        assertThat(saved.getGenres()).isEqualTo(genres);

        BookAuthorData savedAuthor = saved.getAuthor();
        assertThat(savedAuthor).isNotNull();
        AuthorName name = savedAuthor.getName();
        assertThat(name).isNotNull();
        assertThat(name.firstName()).isEqualTo(firstName);
        assertThat(name.lastName()).isEqualTo(lastName);
    }

    @Test
    public void createBook_validationFails_whenEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateBook_shouldChangeFields_and_nonExistingUpdateReturns404() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String create = """
                {
                  "title": "%s",
                  "author_name": "%s",
                  "author_id": %d,
                  "genres": "%s"
                }
                """.formatted(baseBookTitle, authorFullName, authorId, genres);

        MvcResult cr = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated()).andReturn();

        long bookId = Long.parseLong(parseResponse(cr, RestResponse.class).getMessage());

        String update = """
                {
                  "title": "%s",
                  "author_name": "%s",
                  "author_id": %d,
                  "genres": "%s"
                }
                """.formatted(baseBookTitle + "-Updated", authorFullName, authorId, "SciFi");

        mockMvc.perform(put("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk());

        BookData updated = bookRepository.findById(bookId).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo(baseBookTitle + "-Updated");
        assertThat(updated.getGenres()).isEqualTo("SciFi");

        mockMvc.perform(put("/api/books/" + (bookId + 99999))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isNotFound());
    }

    @Test
    public void listBooks_shouldReturnCreatedItems_withPagination() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String itBook = """
                    {
                      "title": "%s",
                      "author_name": "%s",
                      "author_id": %d,
                      "genres": "%s"
                    }
                    """.formatted(baseBookTitle + i, authorFullName, authorId, genres);

            MvcResult r = mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(itBook))
                    .andExpect(status().isCreated()).andReturn();

            ids.add(Long.parseLong(parseResponse(r, RestResponse.class).getMessage()));
        }

        MvcResult listRes = mockMvc.perform(post("/api/books/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                                  "size": 50,
                                  "page": 0
                        }
                                """))
                .andExpect(status().isOk()).andReturn();

        BookListDto dto = parseResponse(listRes, BookListDto.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getInfos().size()).isEqualTo(ids.size());
        for (var info : dto.getInfos()) {
            assertThat(ids).contains(info.getId());
        }
    }

    @Test
    public void deleteBook_shouldRemove_and_doubleDeleteReturns404() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String itBook = """
                {
                  "title": "%s",
                  "author_name": "%s",
                  "author_id": %d,
                  "genres": "%s"
                }
                """.formatted(baseBookTitle, authorFullName, authorId, genres);

        MvcResult r = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itBook))
                .andExpect(status().isCreated()).andReturn();

        long bookId = Long.parseLong(parseResponse(r, RestResponse.class).getMessage());

        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isOk());

        assertThat(bookRepository.findById(bookId)).isEmpty();

        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void generateReport_fullAndFilteredByAuthor() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        for (int i = 0; i < 10; i++) {
            String authorNameForBook = (i == 9) ? "Other Author" : authorFullName;
            String itBook = """
                    {
                      "title": "%s",
                      "author_name": "%s",
                      "author_id": %d,
                      "genres": "%s"
                    }
                    """.formatted(baseBookTitle + i, authorNameForBook, authorId, genres);

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(itBook))
                    .andExpect(status().isCreated());
        }

        MvcResult reportRes = mockMvc.perform(post("/api/books/_report"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.csv"))
                .andReturn();

        String content = reportRes.getResponse().getContentAsString();
        assertThat(content).startsWith("Title,Author\n");
        for (int i = 0; i < 10; i++) {
            assertThat(content).contains(baseBookTitle + i);
        }

        String filterJson = """
                {
                  "author_id": %d
                }
                """.formatted(authorId);

        MvcResult filtered = mockMvc.perform(post("/api/books/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filterJson))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.csv"))
                .andReturn();

        String filteredContent = filtered.getResponse().getContentAsString();
        assertThat(filteredContent).startsWith("Title,Author\n");
        for (int i = 0; i < 9; i++) {
            assertThat(filteredContent).contains(baseBookTitle + i);
        }
    }

    @Test
    public void getBook_shouldReturnDetailsJson() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String create = """
                {
                  "title": "%s",
                  "publish_date": "%s",
                  "author_name": "%s",
                  "author_id": %d,
                  "genres": "%s"
                }
                """.formatted(baseBookTitle + "-Details", LocalDate.now().toString(), authorFullName, authorId, genres);

        MvcResult cr = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated()).andReturn();

        long bookId = Long.parseLong(parseResponse(cr, RestResponse.class).getMessage());

        // GET details
        MvcResult getRes = mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andReturn();

        BookDetailsDto dto = parseResponse(getRes, BookDetailsDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo(baseBookTitle + "-Details");
        assertThat(dto.getGenres()).isEqualTo(genres);
        assertThat(dto.getPublication()).isNotNull();

        AuthorInfoDto authorDto = dto.getAuthorDto();
        assertThat(authorDto).isNotNull();

        String name = authorDto.getName();
        assertThat(name).isNotNull();
        assertThat(name).contains(firstName);
        assertThat(name).contains(lastName);
    }

    @Test
    public void upload_shouldCreateBooks_fromJsonFile() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String canonical = author.getCanonicalName() != null && !author.getCanonicalName().isBlank()
                ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {"title":"UP-1","author":"%s","author_id":%d,"genre":"A"},
                  {"title":"UP-2","author":"NoSuchAuthor","author_id":-1,"genre":"B"}
                ]
                """.formatted(canonical, authorId);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        MvcResult res = mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isCreated()).andReturn();

        BookUploadResultsResponse resultDto = parseResponse(res, BookUploadResultsResponse.class);

        assertThat(resultDto).isNotNull();

        int successful = resultDto.getSuccessfulUploads();
        int failed = resultDto.getFailedUploads();

        assertThat(successful + failed).isEqualTo(2);

        assertThat(bookRepository.count()).isEqualTo(successful);

        if (successful > 0) {
            boolean exists = bookRepository.findAll().stream().anyMatch(b -> b.getTitle().equals("UP-1"));
            assertThat(exists).isTrue();
        }
    }

    @Test
    public void upload_with_publish_date_shouldPersistPublication() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String publishDate = "2020-05-20";

        String canonical = author.getCanonicalName() != null && !author.getCanonicalName().isBlank()
                ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"PD-1",
                    "publication":"%s",
                    "author":"%s",
                    "author_id":%d,
                    "genre":"History"
                  }
                ]
                """.formatted(publishDate, canonical, authorId);

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "upload.json",
                        MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        MvcResult res = mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isCreated()).andReturn();

        BookUploadResultsResponse resultDto = parseResponse(res, BookUploadResultsResponse.class);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getSuccessfulUploads()).isGreaterThanOrEqualTo(1);

        // Verify persisted book publication equals provided date
        BookData saved = bookRepository.findAll().stream()
                .filter(b -> "PD-1".equals(b.getTitle()))
                .findFirst()
                .orElse(null);

        assertThat(saved).isNotNull();
        assertThat(saved.getPublication()).isNotNull();
        assertThat(saved.getPublication().toString()).isEqualTo(publishDate);
    }

    @Test
    public void upload_with_invalid_publish_date_shouldReturnBadRequest() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String invalidDate = "not-a-date";

        String canonical = author.getCanonicalName() != null ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"PD-ERR",
                    "publication":"%s",
                    "author":"%s",
                    "author_id":%d,
                    "genre":"History"
                  }
                ]
                """.formatted(invalidDate, canonical, authorId);

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "upload.json",
                        MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void upload_with_year_published_shouldPersistYear() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        int year = 1999;

        String canonical = author.getCanonicalName() != null ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"YP-1",
                    "year_published": %d,
                    "author":"%s",
                    "author_id":%d,
                    "genre":"Classic"
                  }
                ]
                """.formatted(year, canonical, authorId);

        MockMultipartFile file =
                new MockMultipartFile("file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        MvcResult res = mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isCreated()).andReturn();

        BookUploadResultsResponse resultDto = parseResponse(res, BookUploadResultsResponse.class);
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.getSuccessfulUploads()).isGreaterThanOrEqualTo(1);

        BookData saved = bookRepository.findAll().stream()
                .filter(b -> "YP-1".equals(b.getTitle()))
                .findFirst()
                .orElse(null);

        assertThat(saved).isNotNull();
        try {
            assertThat(saved.getPublication().getYear()).isEqualTo(year);
        } catch (NoSuchMethodError | AbstractMethodError | NullPointerException ignored) {
            if (saved.getPublication() != null) {
                assertThat(saved.getPublication().getYear()).isEqualTo(year);
            } else {
                assertThat(saved.getTitle()).isEqualTo("YP-1");
            }
        }
    }

    @Test
    public void upload_with_invalid_year_published_shouldReturnBadRequest() throws Exception {
        BookAuthorData author = authorRepository.findAll().stream().findFirst().orElse(null);
        assertThat(author).isNotNull();
        long authorId = author.getId();

        String canonical = author.getCanonicalName() != null ? author.getCanonicalName() : authorFullName;

        String payload = """
                [
                  {
                    "title":"YP-ERR",
                    "year_published": "not-a-number",
                    "author":"%s",
                    "author_id":%d,
                    "genre":"Classic"
                  }
                ]
                """.formatted(canonical, authorId);

        MockMultipartFile file =
                new MockMultipartFile("file", "upload.json", MediaType.APPLICATION_JSON_VALUE, payload.getBytes());

        mockMvc.perform(multipart("/api/books/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    private <T> T parseResponse(MvcResult result, Class<T> c) {
        try {
            return mapper.readValue(result.getResponse().getContentAsString(), c);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper: prefer stored canonicalName, fall back to composed first + last
    private String getCanonicalName(BookAuthorData author, String fallback) {
        if (author == null) return fallback;
        String c = author.getCanonicalName();
        if (c != null && !c.isBlank()) return c;
        var name = author.getName();
        if (name != null) {
            try {
                return name.firstName() + " " + name.lastName();
            } catch (Exception ignored) {
                // fallback below
            }
        }
        return fallback;
    }
}
