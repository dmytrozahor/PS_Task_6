package com.dmytrozah.profitsoft.domain.entity.mapper;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorNameDto;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorName;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AuthorNameMapper {

    AuthorNameDto toDto(AuthorName name);

    AuthorName toEntity(AuthorNameDto dto);

    @Named("nameToStringMapper")
    default String nameToStringMapper(AuthorName name) {
        return name == null ? null : name.firstName() + " " + name.lastName();
    }

    @Named("nameToStringMapper")
    default String nameToStringMapper(AuthorNameDto nameDto) {
        return nameDto == null ? null : nameDto.firstName() + " " + nameDto.lastName();
    }

    @Named("stringToNameMapper")
    default AuthorName stringToNameMapper(String name) {
        final String[] split = name.split(" ");

        if (split.length != 2) {
            throw new IllegalArgumentException(name + " is invalid");
        }

        return new AuthorName(split[0], split[1]);
    }

    @Named("toCanonicalName")
    default String toCanonicalName(AuthorNameDto dto) {
        if (dto == null) return null;
        return dto.firstName() + " " + dto.lastName();
    }

    @Named("toCanonicalName")
    default String toCanonicalName(AuthorName name) {
        if (name == null) return null;
        return name.firstName() + " " + name.lastName();
    }
}
