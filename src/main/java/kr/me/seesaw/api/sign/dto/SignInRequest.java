package kr.me.seesaw.api.sign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignInRequest(
        @NotBlank String username,
        @NotBlank String password
) {

}
