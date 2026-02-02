package com.dmytrozah.profitsoft.service.impl;

import com.dmytrozah.profitsoft.domain.dto.author.*;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.mapper.AuthorMapper;
import com.dmytrozah.profitsoft.domain.repository.BookAuthorRepository;
import com.dmytrozah.profitsoft.domain.repository.BookRepository;
import com.dmytrozah.profitsoft.service.BookAuthorService;
import com.dmytrozah.profitsoft.service.NotificationService;
import com.dmytrozah.profitsoft.service.exception.AuthorExistsByName;
import com.dmytrozah.profitsoft.service.exception.AuthorNotFoundException;
import com.dmytrozah.profitsoft.service.exception.InvalidAuthorNameException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements BookAuthorService {
    private final NotificationService notificationService;

    private final BookAuthorRepository repository;
    private final BookRepository bookRepository;

    private final AuthorMapper authorMapper;

    @Override
    public long createAuthor(final AuthorSaveDto saveDto) {
        final AuthorNameDto nameDto = saveDto.getName();

        String canonicalName = nameDto.firstName() + " " + nameDto.lastName();

        if (repository.existsByCanonicalNameIgnoreCase(canonicalName)) {
            throw new AuthorExistsByName(saveDto);
        }

        this.validateAuthor(saveDto);

        notificationService.sendSaveMessage(saveDto);

        return repository.save(fromDto(saveDto)).getId();
    }

    @Override
    public void updateAuthor(long id, final AuthorSaveDto saveDto) {
        this.validateAuthor(saveDto);

        BookAuthorData author = getOrThrow(id);

        this.authorMapper.updateFromDto(saveDto, author);

        repository.save(author);
        repository.flush();
    }

    @Override
    public AuthorDetailsDto resolveAuthorDetails(long id){
        return authorMapper.toDetailsDto(getOrThrow(id), bookRepository);
    }

    @Override
    public AuthorDetailsDto resolveAuthorDetails(String canonicalName) {
        return authorMapper.toDetailsDto(getOrThrow(canonicalName), bookRepository);
    }

    @Override
    public AuthorListDto query(final AuthorQueryDto queryDto) {
        final Page<BookAuthorData> page = repository.findAll(
                PageRequest.of(queryDto.getPage(), queryDto.getSize())
        );

        final List<AuthorInfoDto> infos = page.getContent().stream()
                .map(authorMapper::toInfoDto).toList();

        return AuthorListDto.builder()
                .totalPages(page.getTotalPages())
                .list(infos)
                .build();
    }

    @Override
    public void delete(long id) {
        repository.delete(getOrThrow(id));
    }

    private BookAuthorData fromDto(AuthorSaveDto authorSaveDto) {
        return authorMapper.toEntity(authorSaveDto);
    }

    private BookAuthorData getOrThrow(String canonicalName) {
        return repository.findByCanonicalName(canonicalName)
                .orElseThrow(() -> new AuthorNotFoundException(canonicalName));
    }

    private BookAuthorData getOrThrow(long id){
        return repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    private void validateAuthor(final AuthorSaveDto dto){
        if (dto.getName().firstName().isBlank() || dto.getName().lastName().isBlank()) {
            throw new InvalidAuthorNameException(dto.getName());
        }
    }
}
