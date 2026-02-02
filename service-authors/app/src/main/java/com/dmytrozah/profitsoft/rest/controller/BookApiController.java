package com.dmytrozah.profitsoft.rest.controller;

import com.dmytrozah.profitsoft.domain.dto.ReportGenerationDto;
import com.dmytrozah.profitsoft.domain.dto.RestResponse;
import com.dmytrozah.profitsoft.domain.dto.book.BookDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookListDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookSaveDto;
import com.dmytrozah.profitsoft.domain.dto.book.query.BookQueryDto;
import com.dmytrozah.profitsoft.service.BookService;
import com.dmytrozah.profitsoft.service.BookUploadService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookApiController {
    private final BookService bookService;

    private final BookUploadService uploadService;

    @PostMapping("_list")
    public BookListDto list(@RequestBody BookQueryDto queryDto){
        return bookService.listQuery(queryDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createBook(@Valid @RequestBody BookSaveDto bookSaveDto){
        return RestResponse.builder()
                .message(Long.toString(bookService.createBook(bookSaveDto)))
                .build();
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestResponse updateBook(@PathVariable long id, @Valid @RequestBody BookSaveDto dto){
        bookService.updateBook(id, dto);

        return RestResponse.builder().message("OK").build();
    }

    @GetMapping("{id}")
    public BookDetailsDto getBook(@PathVariable long id){
        return bookService.getBook(id);
    }

    // MultiPartFile <-> Form data
    @PostMapping("upload")
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse uploadFromFile(@RequestParam("file") final MultipartFile file)
            throws FileUploadException {
        return uploadService.uploadFromFile(file);
    }

    @PostMapping(value = "_report", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void generateReport(@RequestBody(required = false) ReportGenerationDto dto, HttpServletResponse response) {
        bookService.generateReport(dto, response);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id){
        bookService.delete(id);
    }
}
