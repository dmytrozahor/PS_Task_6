package com.dmytrozah.profitsoft.domain.entity.mapper;

import com.dmytrozah.profitsoft.domain.dto.author.AuthorAddressDto;
import com.dmytrozah.profitsoft.domain.entity.embeds.AuthorLivingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LivingAddressMapper {

    AuthorAddressDto toDto(AuthorLivingAddress livingAddress);

    AuthorLivingAddress toEntity(AuthorAddressDto addressDto);


}
