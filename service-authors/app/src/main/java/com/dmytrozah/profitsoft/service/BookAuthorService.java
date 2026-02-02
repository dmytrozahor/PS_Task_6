package com.dmytrozah.profitsoft.service;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorListDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorQueryDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;

public interface BookAuthorService {

    long createAuthor(AuthorSaveDto dto);

    void updateAuthor(long id, AuthorSaveDto saveDto);

    AuthorDetailsDto resolveAuthorDetails(long id);

    AuthorDetailsDto resolveAuthorDetails(String canonicalName);

    AuthorListDto query(AuthorQueryDto dto);

    void delete(long id);
}
