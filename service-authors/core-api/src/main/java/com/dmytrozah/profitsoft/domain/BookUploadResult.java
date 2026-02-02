package com.dmytrozah.profitsoft.domain;

import com.dmytrozah.profitsoft.domain.dto.book.upload.UploadResultOutcome;
import com.dmytrozah.profitsoft.domain.entity.BookData;

public record BookUploadResult(BookData book, UploadResultOutcome outcome) {
}
