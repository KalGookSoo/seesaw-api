package kr.me.seesaw.command;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * 토큰 갱신 요청 명령
 */
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshCommand implements Serializable {
    @NotBlank
    private String refreshToken;
}