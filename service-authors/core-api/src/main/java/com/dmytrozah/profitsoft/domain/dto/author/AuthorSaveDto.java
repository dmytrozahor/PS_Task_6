package com.dmytrozah.profitsoft.domain.dto.author;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorSaveDto {

    @JsonProperty("name")
    @NotNull
    private AuthorNameDto name;

    @JsonProperty("address")
    private AuthorAddressDto address;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    @Email
    private String email;

}
