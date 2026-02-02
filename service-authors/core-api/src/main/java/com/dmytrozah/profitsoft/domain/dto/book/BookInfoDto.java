package com.dmytrozah.profitsoft.domain.dto.book;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Short information on {@link com.dmytrozah.profitsoft.domain.entity.BookData}
 * to display in a list
 */

@Getter
@Builder
@AllArgsConstructor
public class BookInfoDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("full_author_name")
    private String authorCanonicalName;

    @JsonProperty("author_id")
    private long authorId;

}
