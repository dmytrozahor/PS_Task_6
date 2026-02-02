package com.dmytrozah.profitsoft.email.dto;

import java.util.List;

public record EmailRequestDto(
        String subject,
        String content,
        List<String> recipients
) {
}
