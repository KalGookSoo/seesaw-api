package kr.me.seesaw.api.security.dto;

import lombok.Builder;

@Builder
public record JsonWebToken(String accessToken, String refreshToken, long expiresIn) {

}
