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

}
