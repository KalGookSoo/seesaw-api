package kr.me.seesaw.api.sign.presentation;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.me.seesaw.api.security.dto.AuthenticatedUserResponse;
import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.security.dto.TokenRefreshRequest;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.api.sign.NaverAccessTokenService;
import kr.me.seesaw.api.sign.dto.NaverAccessTokenRequest;
import kr.me.seesaw.api.sign.dto.SignInRequest;
import kr.me.seesaw.framework.web.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SignApiController {

    // 리프레시 토큰 만료(14일)와 동일하게 맞춘다. JwtTokenProvider의 만료 상수는 private이라 여기서는 별도로 둔다.
    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 60L * 60 * 24 * 14;

    private final AuthenticationService authenticationService;

    private final NaverAccessTokenService naverAccessTokenService;

    @PostMapping("/sign-in")
    public ResponseEntity<JsonWebToken> signIn(@Valid @RequestBody SignInRequest command, HttpServletResponse response) {
        JsonWebToken result = authenticationService.authenticate(command);
        setAuthCookies(response, result);
        return ResponseEntity.ok(result);
    }

    /**
     * 클라이언트가 자체적으로 완료한 네이버 로그인의 액세스 토큰을 검증하고, 대응하는 계정으로 JWT를 발급합니다.
     *
     * @param command 네이버 액세스 토큰 요청
     * @return JWT 토큰이 포함된 응답
     */
    @PostMapping("/oauth2/naver/token")
    public ResponseEntity<JsonWebToken> authenticateNaverAccessToken(@Valid @RequestBody NaverAccessTokenRequest command, HttpServletResponse response) {
        JsonWebToken result = naverAccessTokenService.authenticate(command.accessToken(), command.siteId());
        setAuthCookies(response, result);
        return ResponseEntity.ok(result);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * <p>
     * 리프레시 토큰은 요청 바디(하위 호환) 또는 SEESAW_REFRESH_TOKEN 쿠키로 전달할 수 있다.
     *
     * @param command            토큰 갱신 요청(선택, 쿠키로 전달하는 경우 생략 가능)
     * @param refreshTokenCookie 쿠키로 전달된 리프레시 토큰(선택)
     * @return 새로운 JWT 토큰이 포함된 응답
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<JsonWebToken> refreshToken(
            @Valid @RequestBody(required = false) TokenRefreshRequest command,
            @CookieValue(value = JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
            HttpServletResponse response
    ) {
        String refreshToken = command != null && command.refreshToken() != null ? command.refreshToken() : refreshTokenCookie;
        JsonWebToken result = authenticationService.refreshToken(refreshToken);
        setAuthCookies(response, result);
        return ResponseEntity.ok(result);
    }

    /**
     * 현재 인증된 사용자 본인의 정보를 조회합니다. httpOnly 쿠키만 갖고 있어 JWT를 직접 읾을 수 없는
     * 클라이언트가 로그인 상태/사용자 정보를 확인하는 용도.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> getCurrentUser() {
        return ResponseEntity.ok(authenticationService.getCurrentUser());
    }

    /**
     * 인증 쿠키(SEESAW_ACCESS_TOKEN, SEESAW_REFRESH_TOKEN)를 제거합니다.
     * JWT는 상태가 없어 서버가 별도로 무효화할 대상이 없고, httpOnly 쿠키는 JS로 지울 수 없어
     * 로그아웃 시 서버가 만료된 쿠키를 다시 내려줘야 한다.
     */
    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    private void setAuthCookies(HttpServletResponse response, JsonWebToken token) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(
                JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, token.accessToken(), token.expiresIn() / 1000
        ).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(
                JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME, token.refreshToken(), REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS
        ).toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME, "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildAuthCookie(JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME, "", 0).toString());
    }

    private ResponseCookie buildAuthCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

}
