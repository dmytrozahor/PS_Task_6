package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthorInfoDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

}
