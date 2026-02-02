package com.dmytrozah.profitsoft.rest.controller;

import com.dmytrozah.profitsoft.domain.dto.RestResponse;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorListDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorQueryDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;
import com.dmytrozah.profitsoft.service.BookAuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorApiController {
    private final BookAuthorService authorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse createAuthor(@Valid @RequestBody AuthorSaveDto saveDto){
        return RestResponse.builder()
                .message(Long.toString(authorService.createAuthor(saveDto)))
                .build();
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestResponse updateAuthor(@PathVariable long id, @Valid @RequestBody AuthorSaveDto saveDto) {
        authorService.updateAuthor(id, saveDto);

        return RestResponse.builder().message("OK").build();
    }

    @GetMapping("{id}")
    public AuthorDetailsDto getAuthor(@PathVariable Long id){
        return authorService.resolveAuthorDetails(id);
    }

    @PostMapping({"_list"})
    public AuthorListDto getAuthors(@Valid @RequestBody AuthorQueryDto queryDto) {
        return authorService.query(queryDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestResponse deleteAuthor(@PathVariable final Long id){
        authorService.delete(id);

        return RestResponse.builder().message("OK").build();
    }

}
