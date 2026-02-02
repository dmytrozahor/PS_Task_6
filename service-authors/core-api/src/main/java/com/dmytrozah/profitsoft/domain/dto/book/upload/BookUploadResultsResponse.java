package com.dmytrozah.profitsoft.domain.dto.book.upload;

import com.dmytrozah.profitsoft.domain.dto.RestResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class BookUploadResultsResponse extends RestResponse {
    private final List<BookUploadResultDto> results;

    private final int successfulUploads;

    private final int failedUploads;

    public BookUploadResultsResponse(List<BookUploadResultDto> results,
                                     int successfulUploads,
                                     int failedUploads) {
        super(successfulUploads > 0 ?
                " %d books were successfully uploaded (%d failures)".formatted(successfulUploads, failedUploads)
                : "No books could be uploaded."
        );

        this.results = results;
        this.successfulUploads = successfulUploads;
        this.failedUploads = failedUploads;
    }
}
