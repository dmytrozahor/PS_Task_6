package com.dmytrozah.profitsoft.adapter.amqp;

import com.dmytrozah.profitsoft.adapter.amqp.dto.EmailRequestDto;

public interface EmailServiceAdapter {

    void requestEmail(EmailRequestDto dto);

}
