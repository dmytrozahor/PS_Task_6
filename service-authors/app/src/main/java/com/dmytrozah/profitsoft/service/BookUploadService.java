package com.dmytrozah.profitsoft.service;

import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultsResponse;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface BookUploadService {

    BookUploadResultsResponse uploadFromFile(final MultipartFile file) throws FileUploadException;

}
