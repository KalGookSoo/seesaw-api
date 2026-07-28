package kr.me.seesaw.api.sign;

import kr.me.seesaw.api.security.dto.JsonWebToken;

public interface NaverAccessTokenService {

    /**
     * 네이버 액세스 토큰을 검증하고, 대응하는 계정에 해당 사이트의 역할을 부여한 뒤 그 역할이 반영된 JWT를 발급합니다.
     *
     * @param accessToken 네이버 액세스 토큰
     * @param siteId      인증을 요청한 사이트 식별자
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken authenticate(String accessToken, String siteId);

}
