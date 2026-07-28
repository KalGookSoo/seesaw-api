package kr.me.seesaw.api.sign;

import kr.me.seesaw.api.security.dto.JsonWebToken;

public interface NaverAccessTokenService {

    /**
     * 네이버 액세스 토큰을 검증하고, 대응하는 계정으로 JWT를 발급합니다.
     *
     * @param accessToken 네이버 액세스 토큰
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken authenticate(String accessToken);

}
