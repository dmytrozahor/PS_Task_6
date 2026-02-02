package com.dmytrozah.profitsoft.domain.dto.book.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookQueryDtoFilter(@JsonProperty("attribute") BookDtoAttribute attribute,
                                 @JsonProperty("value") Object value) {
}
