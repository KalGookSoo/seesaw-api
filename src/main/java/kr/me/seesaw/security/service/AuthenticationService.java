package kr.me.seesaw.security.service;

import kr.me.seesaw.security.dto.SignInRequest;
import kr.me.seesaw.security.dto.SignInResponse;

public interface AuthenticationService {

    /**
     * 사용자 인증 및 JWT 토큰 발급
     * @param request 로그인 요청 정보
     * @return JWT 토큰이 포함된 응답
     */
    SignInResponse authenticate(SignInRequest request);
}
