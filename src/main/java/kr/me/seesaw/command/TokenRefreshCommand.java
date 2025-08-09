package kr.me.seesaw.command;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 토큰 갱신 요청 명령
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshCommand {
    
    @NotBlank
    private String refreshToken;
}