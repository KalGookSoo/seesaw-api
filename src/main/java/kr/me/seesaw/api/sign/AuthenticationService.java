package kr.me.seesaw.api.sign;

import kr.me.seesaw.api.sign.dto.SignInRequest;
import kr.me.seesaw.api.security.dto.AuthenticatedUserResponse;
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

    /**
     * 이미 신원이 확인된 사용자명으로 JWT 토큰을 발급
     * @param username 사용자명
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken issueToken(String username);

    /**
     * 이미 신원이 확인된 사용자명으로, 특정 사이트에서 행사할 수 있는 역할만 담아 JWT 토큰을 발급
     * @param username 사용자명
     * @param siteId 사이트 식별자
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken issueToken(String username, String siteId);

    /**
     * 현재 인증된 사용자 본인의 정보를 조회합니다.
     * @return 계정 식별자/사용자명/권한이 담긴 응답
     */
    AuthenticatedUserResponse getCurrentUser();
}
