package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorNameDto(
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName
) {
}
