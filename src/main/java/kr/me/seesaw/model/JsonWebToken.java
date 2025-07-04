package kr.me.seesaw.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JsonWebToken {

    private String accessToken;

    private String refreshToken;

    private long expiresIn;

    /**
     * 이전 버전과의 호환성을 위한 생성자
     */
    public JsonWebToken(String token) {
        this.accessToken = token;
        this.refreshToken = null;
        this.expiresIn = 0;
    }

}
