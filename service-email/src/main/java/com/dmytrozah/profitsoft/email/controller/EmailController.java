package com.dmytrozah.profitsoft.email.controller;

import com.dmytrozah.profitsoft.email.dto.EmailStatusDto;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import com.dmytrozah.profitsoft.email.service.EmailQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailQueryService queryService;

    @GetMapping
    public Page<EmailStatusDto> getAllEmails(Pageable pageable) {
        return queryService.findAll(pageable);
    }

    @GetMapping("/status/{status}")
    public Page<EmailStatusDto> getEmailsByStatus(
            @PathVariable EmailStatus status,
            Pageable pageable
    ) {
        return queryService.findByStatus(status, pageable);
    }

    @GetMapping("/{id}")
    public EmailStatusDto getEmailById(@PathVariable String id) {
        return queryService.findById(id);
    }
}
