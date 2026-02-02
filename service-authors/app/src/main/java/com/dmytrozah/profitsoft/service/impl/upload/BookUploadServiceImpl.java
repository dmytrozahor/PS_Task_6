package com.dmytrozah.profitsoft.service.impl.upload;

import com.dmytrozah.profitsoft.domain.BookUploadResult;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadDto;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultDto;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultsResponse;
import com.dmytrozah.profitsoft.domain.dto.book.upload.UploadResultOutcome;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.BookData;
import com.dmytrozah.profitsoft.domain.entity.mapper.BookUploadMapper;
import com.dmytrozah.profitsoft.domain.repository.BookAuthorRepository;
import com.dmytrozah.profitsoft.domain.repository.BookRepository;
import com.dmytrozah.profitsoft.service.BookUploadService;
import com.dmytrozah.profitsoft.service.exception.BookNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookUploadServiceImpl implements BookUploadService {
    static final ObjectMapper mapper = new ObjectMapper();

    private final BookUploadMapper uploadMapper;

    private final BookRepository bookRepository;

    private final BookAuthorRepository authorRepository;

    @Override
    public BookUploadResultsResponse uploadFromFile(final MultipartFile file) throws FileUploadException {
        try {
            byte[] bytes = file.getBytes();

            List<BookUploadDto> uploads = mapper.readValue(bytes, new TypeReference<>() {
            });
            List<BookUploadResult> results = uploads.stream()
                    .map(this::convertFromUpload)
                    .toList();

            List<BookData> bulkSave = results.stream()
                    .filter(res -> res.outcome().equals(UploadResultOutcome.SUCCESS))
                    .map(BookUploadResult::book)
                    .toList();

            bulkSave = bookRepository.saveAll(bulkSave);

            List<BookUploadResultDto> resultDtos = results.stream()
                    .map(this.uploadMapper::toDto)
                    .toList();

            final int successfulUploads = bulkSave.size();

            return new BookUploadResultsResponse(
                    resultDtos,
                    successfulUploads,
                    uploads.size() - successfulUploads
            );
        } catch (IOException e) {
            throw new FileUploadException(e.getMessage());
        }
    }

    private BookUploadResult convertFromUpload(BookUploadDto bookUploadDto) throws BookNotFoundException {
        BookData bookData = new BookData();

        bookData.setTitle(bookUploadDto.getTitle());
        bookData.setAuthorCanonicalName(bookUploadDto.getAuthorName());

        if (!authorRepository.existsByCanonicalNameIgnoreCase(bookUploadDto.getAuthorName())) {
            return new BookUploadResult(bookData, UploadResultOutcome.AUTHOR_NOT_FOUND);
        }

        if (bookRepository.existsByTitleAndAuthorCanonicalName(
                bookUploadDto.getTitle(), bookUploadDto.getAuthorName())
        ) {
            bookData = bookRepository.findAllByAuthorCanonicalNameAndTitle(
                    bookUploadDto.getAuthorName(),
                    bookUploadDto.getTitle()
            ).getFirst();

            return new BookUploadResult(bookData, UploadResultOutcome.TITLE_AUTHOR_EXISTS);
        }

        BookAuthorData author = null;

        if (bookUploadDto.getAuthorName() != null) {
            author = authorRepository.findByCanonicalName(bookUploadDto.getAuthorName())
                    .orElse(null);
        }

        if (bookUploadDto.getAuthorId() != -1) {
            author = authorRepository.findById(bookUploadDto.getAuthorId())
                    .orElse(null);
        }

        if (author == null) {
            return new BookUploadResult(
                    bookData,
                    UploadResultOutcome.AUTHOR_NOT_FOUND
            );
        }

        bookData.setAuthor(author);
        bookData.setGenres(bookUploadDto.getGenres());

        if (bookUploadDto.getPublication() != null) {
            bookData.setPublication(bookUploadDto.getPublication());
        } else if (bookUploadDto.getPublicationYear() != -1) {
            bookData.setPublication(
                    LocalDate.of(bookUploadDto.getPublicationYear(), 1, 1)
            );
        } else {
            bookData.setPublication(LocalDate.now());
        }

        return new BookUploadResult(bookData, UploadResultOutcome.SUCCESS);
    }
}
