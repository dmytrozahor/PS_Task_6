package com.dmytrozah.profitsoft.domain.dto.book.upload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookUploadDto {

    @JsonProperty("title")
    private String title;

    @JsonProperty("author_id")
    private long authorId = -1;

    @JsonProperty("author")
    private String authorName;

    @JsonProperty("genre")
    private String genres;

    @JsonProperty("publication")
    private LocalDate publication;

    @JsonProperty("year_published")
    private int publicationYear = -1;

}
