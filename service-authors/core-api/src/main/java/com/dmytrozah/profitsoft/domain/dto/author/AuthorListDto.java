package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AuthorListDto {

    @JsonProperty("list")
    private List<AuthorInfoDto> list;

    @JsonProperty("total_pages")
    private int totalPages;

}
