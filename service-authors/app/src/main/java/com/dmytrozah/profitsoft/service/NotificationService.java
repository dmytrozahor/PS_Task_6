package com.dmytrozah.profitsoft.service;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;

public interface NotificationService {

    void sendSaveMessage(final AuthorSaveDto dto);

}
