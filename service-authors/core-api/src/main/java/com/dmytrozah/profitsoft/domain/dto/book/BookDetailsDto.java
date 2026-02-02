package com.dmytrozah.profitsoft.domain.dto.book;


import com.dmytrozah.profitsoft.domain.dto.author.AuthorInfoDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Detailed information on {@link com.dmytrozah.profitsoft.domain.entity.BookData}
 * to display as a card
 */

@AllArgsConstructor
@Getter @Setter
@Builder
public class BookDetailsDto {
    @JsonProperty("id")
    private long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("publication_date")
    private LocalDate publication;

    @JsonProperty("last_update_time")
    private Instant lastUpdateTime;

    @JsonProperty("author")
    private AuthorInfoDto authorDto;

    private String genres;
}
