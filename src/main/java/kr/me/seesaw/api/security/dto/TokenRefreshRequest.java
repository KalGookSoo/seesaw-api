package kr.me.seesaw.api.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * 토큰 갱신 요청 명령
 */
@Builder
public record TokenRefreshRequest(@NotBlank String refreshToken) {

}
