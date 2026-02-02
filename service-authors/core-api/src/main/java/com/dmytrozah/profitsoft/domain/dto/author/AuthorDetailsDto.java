package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AuthorDetailsDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private AuthorNameDto name;

    @JsonProperty("canonical_name")
    private String canonicalName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("address")
    private AuthorAddressDto address;

    @JsonProperty("books_published")
    private int books;

}
