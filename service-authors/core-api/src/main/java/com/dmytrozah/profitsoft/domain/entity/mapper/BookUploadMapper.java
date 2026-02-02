package com.dmytrozah.profitsoft.domain.entity.mapper;

import com.dmytrozah.profitsoft.domain.BookUploadResult;
import com.dmytrozah.profitsoft.domain.dto.book.upload.BookUploadResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = {BookMapper.class})
public interface BookUploadMapper {

    @Mapping(target = "bookInfo", source = "book")
    BookUploadResultDto toDto(BookUploadResult bookUploadResult);
}
