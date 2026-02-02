package com.dmytrozah.profitsoft.domain.entity.mapper;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorInfoDto;
import com.dmytrozah.profitsoft.domain.dto.author.AuthorSaveDto;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.repository.BookRepository;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LivingAddressMapper.class, AuthorNameMapper.class}
)
public interface AuthorMapper {

    @Mapping(target = "name", source = "name", qualifiedByName = "nameToStringMapper")
    AuthorInfoDto toInfoDto(BookAuthorData author);

    @Mapping(target = "address", source = "postalAddress")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "canonicalName", source = "name", qualifiedByName = "nameToStringMapper")
    @Mapping(target = "books", source = "author", qualifiedByName = "computeBookCount")
    AuthorDetailsDto toDetailsDto(BookAuthorData author, @Context BookRepository bookRepository);

    @Mapping(target = "canonicalName", source = "name", qualifiedByName = "nameToStringMapper")
    @Mapping(target = "postalAddress", source = "address")
    void updateFromDto(AuthorSaveDto saveDto, @MappingTarget BookAuthorData data);

    @Mapping(target = "postalAddress", source = "address")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "canonicalName", source = "name", qualifiedByName = "nameToStringMapper")
    BookAuthorData toEntity(AuthorSaveDto saveDto);

    @Mapping(target = "postalAddress", source = "address")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "canonicalName", source = "name", qualifiedByName = "nameToStringMapper")
    @Mapping(target = "books", ignore = true)
    BookAuthorData toEntity(AuthorDetailsDto detailsDto);

    @Named("computeBookCount")
    default int computeBookCount(BookAuthorData author, @Context BookRepository bookRepository) {
        return bookRepository.countByAuthorId(author.getId());
    }


}
