package com.dmytrozah.profitsoft.domain.dto.book;

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
public class BookListDto {

    @JsonProperty("list")
    private List<BookInfoDto> infos;

    @JsonProperty("total_pages")
    private int totalPages;

}
