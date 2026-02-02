package com.dmytrozah.profitsoft.email.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "email_messages")
@Data
@Builder
public class EmailMessage {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private List<String> recipients;

    private String subject;
    private String content;

    @Field(type = FieldType.Keyword)
    private EmailStatus status;

    private String errorMessage;
    private Integer retryAttempt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime lastAttemptAt;
}