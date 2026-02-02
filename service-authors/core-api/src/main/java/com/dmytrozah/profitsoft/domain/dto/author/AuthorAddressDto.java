package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorAddressDto(String street,
                               @JsonProperty("house_number")
                               int houseNum,
                               String city,
                               String country,
                               @JsonProperty("post_code")
                               String postCode
) {
}
