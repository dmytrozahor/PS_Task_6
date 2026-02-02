package com.dmytrozah.profitsoft.app.test;

import com.dmytrozah.profitsoft.Task2App;
import com.dmytrozah.profitsoft.domain.dto.RestResponse;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorListDto;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorLivingAddress;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorName;
import com.dmytrozah.profitsoft.domain.repository.BookAuthorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = Task2App.class
)
@AutoConfigureMockMvc
class AuthorControllerTest {

    static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookAuthorRepository authorRepository;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String street;
    private int houseNum;
    private String city;
    private String country;
    private String postCode;

    @BeforeEach
    void setup() {
        firstName = "Dmytro";
        lastName = "Zahorodnii";
        email = "dmytrozah.dev@gmail.com";
        phone = "555-0101";
        street = "Main St";
        houseNum = 12;
        city = "Karlsruhe";
        country = "Germany";
        postCode = "76131";
    }

    @AfterEach
    void cleanup() {
        authorRepository.deleteAll();
    }

    private String buildAuthorSaveJson(String first, String last, String emailVal, String phoneVal,
                                       String streetVal, int house, String cityVal, String countryVal, String postCodeVal) {
        return """
                {
                  "name": {
                    "first_name": "%s",
                    "last_name": "%s"
                  },
                  "address": {
                    "street": "%s",
                    "house_number": %d,
                    "city": "%s",
                    "country": "%s",
                    "post_code": %s
                  },
                  "phone_number": "%s",
                  "email": "%s"
                }
                """.formatted(first, last, streetVal, house, cityVal, countryVal, postCodeVal, phoneVal, emailVal);
    }

    @Test
    public void testCreateAuthorNestedAddress() throws Exception {
        String body = buildAuthorSaveJson(firstName, lastName, email, phone, street, houseNum, city, country, postCode);

        MvcResult result = mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated()).andReturn();

        RestResponse response = parseResponse(result, RestResponse.class);
        long authorId = Long.parseLong(response.getMessage());

        assertThat(authorId).isGreaterThanOrEqualTo(1);

        BookAuthorData saved = authorRepository.findById(authorId).orElse(null);
        assertThat(saved).isNotNull();

        AuthorName name = saved.getName();
        assertThat(name).isNotNull();
        assertThat(name.firstName()).isEqualTo(firstName);
        assertThat(name.lastName()).isEqualTo(lastName);

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPhoneNumber()).isEqualTo(phone);

        AuthorLivingAddress addr = saved.getPostalAddress();
        assertThat(addr).isNotNull();
        assertThat(addr.street()).isEqualTo(street);
        assertThat(addr.houseNum()).isEqualTo(houseNum);
        assertThat(addr.city()).isEqualTo(city);
        assertThat(addr.country()).isEqualTo(country);
        assertThat(addr.postCode()).isEqualTo(postCode);
    }

    @Test
    public void testCreateAuthorValidationFails() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateAuthorAndNotFound() throws Exception {
        // create
        String createBody = buildAuthorSaveJson(firstName, lastName, email, phone, street, houseNum, city, country, postCode);
        MvcResult res = mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody)
        ).andExpect(status().isCreated()).andReturn();

        long id = Long.parseLong(parseResponse(res, RestResponse.class).getMessage());

        // update values
        String newFirst = firstName + "-u";
        String newLast = lastName + "-u";
        String newEmail = "new-" + email;
        String newPhone = "999-9999";
        String newStreet = "New St";
        int newHouse = 99;
        String newCity = "Berlin";
        String newCountry = "Germany";
        String newPost = "10115";

        String updateBody = buildAuthorSaveJson(newFirst, newLast, newEmail, newPhone, newStreet, newHouse, newCity, newCountry, newPost);

        mockMvc.perform(put("/api/authors/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
        ).andExpect(status().isOk());

        BookAuthorData updated = authorRepository.findById(id).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getName().firstName()).isEqualTo(newFirst);
        assertThat(updated.getEmail()).isEqualTo(newEmail);
        assertThat(updated.getPhoneNumber()).isEqualTo(newPhone);
        assertThat(updated.getPostalAddress().city()).isEqualTo(newCity);

        // update non-existing -> 404
        mockMvc.perform(put("/api/authors/" + (id + 9999))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void testListAuthorsPaginationAndContent() throws Exception {
        List<Long> ids = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            String body = buildAuthorSaveJson(firstName + i, lastName + i,
                    "e" + i + "@ex.com", "p" + i,
                    street + i, houseNum + i, city + i, country, postCode + i);

            MvcResult res = mockMvc.perform(post("/api/authors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            ).andExpect(status().isCreated()).andReturn();

            RestResponse response = parseResponse(res, RestResponse.class);
            ids.add(Long.parseLong(response.getMessage()));
        }

        MvcResult listRes = mockMvc.perform(post("/api/authors/_list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "size": 50,
                          "page": 0
                        }
                        """)
        ).andExpect(status().isOk()).andReturn();

        AuthorListDto dto = parseResponse(listRes, AuthorListDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getList().size()).isEqualTo(ids.size());

        for (var info : dto.getList()) {
            assertThat(ids).contains(info.getId());
        }
    }

    @Test
    public void testDeleteAuthorSuccessAndNotFound() throws Exception {
        String body = buildAuthorSaveJson(firstName, lastName, email, phone, street, houseNum, city, country, postCode);
        MvcResult res = mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpect(status().isCreated()).andReturn();

        long id = Long.parseLong(parseResponse(res, RestResponse.class).getMessage());

        mockMvc.perform(delete("/api/authors/" + id))
                .andExpect(status().isOk());

        assertThat(authorRepository.findById(id)).isEmpty();

        mockMvc.perform(delete("/api/authors/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAuthor_shouldReturnDetailsJson() throws Exception {
        String body = buildAuthorSaveJson(firstName, lastName, email, phone, street, houseNum, city, country, postCode);

        MvcResult res = mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated()).andReturn();

        long authorId = Long.parseLong(parseResponse(res, RestResponse.class).getMessage());

        MvcResult getRes = mockMvc.perform(get("/api/authors/" + authorId))
                .andExpect(status().isOk())
                .andReturn();

        AuthorDetailsDto dto = parseResponse(getRes, AuthorDetailsDto.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(authorId);
        assertThat(dto.getName()).isNotNull();
        assertThat(dto.getName().firstName()).isEqualTo(firstName);
        assertThat(dto.getName().lastName()).isEqualTo(lastName);

        assertThat(dto.getEmail()).isEqualTo(email);
        assertThat(dto.getPhoneNumber()).isEqualTo(phone);

        assertThat(dto.getAddress()).isNotNull();
        assertThat(dto.getAddress().street()).isEqualTo(street);
        assertThat(dto.getAddress().houseNum()).isEqualTo(houseNum);
        assertThat(dto.getAddress().city()).isEqualTo(city);
        assertThat(dto.getAddress().country()).isEqualTo(country);
        assertThat(dto.getBooks()).isEqualTo(0); // newly created author has no published books yet
    }

    @Test
    public void testCreateAuthorInvalidName_shouldReturnBadRequest() throws Exception {
        // Build payload with invalid/empty author name to trigger InvalidAuthorNameException
        String invalidBody = """
                {
                  "name": {
                    "first_name": "",
                    "last_name": ""
                  },
                  "address": {
                    "street": "%s",
                    "house_number": %d,
                    "city": "%s",
                    "country": "%s",
                    "post_code": %s
                  },
                  "phone_number": "%s",
                  "email": "%s"
                }
                """.formatted(street, houseNum, city, country, postCode, phone, email);

        MvcResult res = mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Response body should contain the InvalidAuthorNameException message
        String responseContent = res.getResponse().getContentAsString();
        assertThat(responseContent).contains("Author name");
        // Optionally ensure it references the invalid (empty) name pattern
        assertThat(responseContent).contains("invalid");
    }

    private <T> T parseResponse(MvcResult result, Class<T> c) {
        try {
            return mapper.readValue(result.getResponse().getContentAsString(), c);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
