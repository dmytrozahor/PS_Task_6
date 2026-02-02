package com.dmytrozah.profitsoft.service.impl;

import com.dmytrozah.profitsoft.adapter.amqp.EmailServiceAdapter;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;
import com.dmytrozah.profitsoft.adapter.amqp.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final EmailServiceAdapter emailServiceAdapter;

    @Override
    public void sendSaveMessage(AuthorSaveDto dto) {
        final EmailRequestDto createdMessage = EmailRequestDto.builder()
                .subject("New Author Created")
                .content("A new author has been successfully created: " + dto.getName())
                .recipients(List.of(dto.getEmail()))
                .build();

        emailServiceAdapter.requestEmail(createdMessage);
    }
}
