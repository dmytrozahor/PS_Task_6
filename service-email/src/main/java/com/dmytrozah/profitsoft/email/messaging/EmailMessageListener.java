package com.dmytrozah.profitsoft.email.messaging;

import com.dmytrozah.profitsoft.email.config.RabbitConfig;
import com.dmytrozah.profitsoft.email.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailMessageListener {
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void handleEmailRequest(EmailRequestDto request) {
        emailService.processEmailRequest(request);
    }
}