package com.dmytrozah.profitsoft.domain.entity.embeds;

import jakarta.persistence.Embeddable;

@Embeddable
public record AuthorName(String firstName, String lastName) {
}
