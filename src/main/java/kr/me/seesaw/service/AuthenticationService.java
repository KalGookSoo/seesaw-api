package kr.me.seesaw.service;

import kr.me.seesaw.command.SignInCommand;
import kr.me.seesaw.model.JsonWebToken;

public interface AuthenticationService {

    /**
     * 사용자 인증 및 JWT 토큰 발급
     * @param request 로그인 요청 정보
     * @return JWT 토큰이 포함된 응답
     */
    JsonWebToken authenticate(SignInCommand request);
}
