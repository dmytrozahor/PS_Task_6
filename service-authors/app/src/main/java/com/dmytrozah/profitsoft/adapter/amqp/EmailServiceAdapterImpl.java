package com.dmytrozah.profitsoft.adapter.amqp;

import com.dmytrozah.profitsoft.adapter.amqp.dto.EmailRequestDto;
import com.dmytrozah.profitsoft.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailServiceAdapterImpl implements EmailServiceAdapter {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void requestEmail(EmailRequestDto dto) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_EMAIL,
                RabbitConfig.ROUTING_KEY_EMAIL,
                dto
        );
    }
}
