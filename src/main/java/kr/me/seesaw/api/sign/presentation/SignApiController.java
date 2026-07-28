package kr.me.seesaw.api.sign.presentation;

import jakarta.validation.Valid;
import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.security.dto.TokenRefreshRequest;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.api.sign.NaverAccessTokenService;
import kr.me.seesaw.api.sign.dto.NaverAccessTokenRequest;
import kr.me.seesaw.api.sign.dto.SignInRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SignApiController {

    private final AuthenticationService authenticationService;

    private final NaverAccessTokenService naverAccessTokenService;

    @PostMapping("/sign-in")
    public ResponseEntity<JsonWebToken> signIn(@Valid @RequestBody SignInRequest command) {
        JsonWebToken response = authenticationService.authenticate(command);
        return ResponseEntity.ok(response);
    }

    /**
     * 클라이언트가 자체적으로 완료한 네이버 로그인의 액세스 토큰을 검증하고, 대응하는 계정으로 JWT를 발급합니다.
     *
     * @param command 네이버 액세스 토큰 요청
     * @return JWT 토큰이 포함된 응답
     */
    @PostMapping("/oauth2/naver/token")
    public ResponseEntity<JsonWebToken> authenticateNaverAccessToken(@Valid @RequestBody NaverAccessTokenRequest command) {
        JsonWebToken response = naverAccessTokenService.authenticate(command.accessToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     *
     * @param command 토큰 갱신 요청
     * @return 새로운 JWT 토큰이 포함된 응답
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<JsonWebToken> refreshToken(@Valid @RequestBody TokenRefreshRequest command) {
        JsonWebToken response = authenticationService.refreshToken(command.refreshToken());
        return ResponseEntity.ok(response);
    }

}
