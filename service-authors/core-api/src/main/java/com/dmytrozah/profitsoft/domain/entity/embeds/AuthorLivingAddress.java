package com.dmytrozah.profitsoft.domain.entity.embeds;

import jakarta.persistence.Embeddable;

@Embeddable
public record AuthorLivingAddress(String postCode, int houseNum, String street, String city, String country) {
}

