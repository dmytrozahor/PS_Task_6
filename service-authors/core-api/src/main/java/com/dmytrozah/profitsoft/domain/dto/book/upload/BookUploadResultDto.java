package com.dmytrozah.profitsoft.domain.dto.book.upload;

import com.dmytrozah.profitsoft.domain.dto.book.BookInfoDto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BookUploadResultDto(@JsonProperty("info") BookInfoDto bookInfo,
                                  @JsonProperty("outcome") UploadResultOutcome outcome) {
}
