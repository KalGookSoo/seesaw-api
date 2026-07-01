package kr.me.seesaw.api.sign;

import kr.me.seesaw.api.sign.dto.SignInRequest;
import kr.me.seesaw.api.security.dto.JsonWebToken;

public interface AuthenticationService {

    /**
     * 사용자 인증 및 JWT 토큰 발급
     * @param command 로그인 요청 정보
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken authenticate(SignInRequest command);

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * @param refreshToken 리프레시 토큰
     * @return 새로운 JWT 토큰이 포함된 응답
     */
    JsonWebToken refreshToken(String refreshToken);
}
