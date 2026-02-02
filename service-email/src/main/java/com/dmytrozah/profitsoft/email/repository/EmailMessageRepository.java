package com.dmytrozah.profitsoft.email.repository;

import com.dmytrozah.profitsoft.email.model.EmailMessage;
import com.dmytrozah.profitsoft.email.model.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EmailMessageRepository
        extends ElasticsearchRepository<EmailMessage, String> {

    List<EmailMessage> findByStatus(EmailStatus status);

    Page<EmailMessage> findByStatus(EmailStatus status, Pageable pageable);

}
