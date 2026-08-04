package kr.me.seesaw.api.security.dto;

import lombok.Builder;
import org.springframework.lang.NonNull;

@Builder
public record JsonWebToken(String accessToken, String refreshToken, long expiresIn) {

    @NonNull
    @Override
    public String toString() {
        return "JsonWebToken{accessToken=[REDACTED], refreshToken=[REDACTED], expiresIn=" + expiresIn + "}";
    }

}
