package com.dmytrozah.profitsoft.service;

import com.dmytrozah.profitsoft.domain.dto.ReportGenerationDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookListDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookSaveDto;
import com.dmytrozah.profitsoft.domain.dto.book.query.BookQueryDto;
import jakarta.servlet.http.HttpServletResponse;

public interface BookService {

    long createBook(BookSaveDto bookSaveDto);

    void updateBook(long id, BookSaveDto dto);

    BookDetailsDto getBook(long id);

    BookListDto listQuery(final BookQueryDto queryDto);

    void delete(long id);

    void generateReport(ReportGenerationDto dto, HttpServletResponse response);
}
