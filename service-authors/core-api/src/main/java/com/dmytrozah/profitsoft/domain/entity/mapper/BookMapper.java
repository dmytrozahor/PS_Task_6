package com.dmytrozah.profitsoft.domain.entity.mapper;

import com.dmytrozah.profitsoft.domain.dto.book.BookDetailsDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookInfoDto;
import com.dmytrozah.profitsoft.domain.dto.book.BookSaveDto;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadDto;
import com.dmytrozah.profitsoft.domain.entity.BookAuthorData;
import com.dmytrozah.profitsoft.domain.entity.BookData;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorName;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = {AuthorMapper.class})
public interface BookMapper {

    @Mapping(target = "author.id", source = "authorId")
    @Mapping(target = "authorCanonicalName", source = "authorName")
    @Mapping(target = "lastUpdateTime", ignore = true)
    BookData toEntity(BookUploadDto uploadDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author.id", source = "authorId")
    @Mapping(target = "lastUpdateTime", ignore = true)
    @Mapping(target = "publication", source = "publishDate")
    @Mapping(target = "authorCanonicalName", source = "authorName")
    BookData toEntity(BookSaveDto saveDto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorCanonicalName", source = "authorCanonicalName")
    BookInfoDto toInfoDto(BookData data);

    @Mapping(target = "authorDto", source = "author")
    @Mapping(target = "publication", source = "publication")
    BookDetailsDto toDetailsDto(BookData data);

    @Mapping(target = "authorCanonicalName",
            source = "saveDto",
            qualifiedByName = "computeCanonicalName"
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdateTime", ignore = true)
    @Mapping(target = "publication", source = "publishDate")
    @Mapping(target = "author", ignore = true)
    void updateEntityFromDto(BookSaveDto saveDto,
                             @MappingTarget BookData entity,
                             @Context BookAuthorData data);

    @Named("computeCanonicalName")
    default String computeCanonicalName(BookSaveDto saveDto, @Context BookAuthorData data) {
        AuthorName name = data.getName();

        return name == null ? null : name.firstName() + " " + name.lastName();
    }
}
