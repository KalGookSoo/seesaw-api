package kr.me.seesaw.api.sign.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import kr.me.seesaw.api.security.dto.AuthenticatedUserResponse;
import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.security.dto.TokenRefreshRequest;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.api.sign.NaverAccessTokenService;
import kr.me.seesaw.api.sign.dto.NaverAccessTokenRequest;
import kr.me.seesaw.api.sign.dto.SignInRequest;
import kr.me.seesaw.framework.web.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SignApiControllerTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private NaverAccessTokenService naverAccessTokenService;

    private static final String CSRF_TOKEN = "test-csrf-token";

    // CookieCsrfTokenRepository의 기본 네이밍(비공개 상수라 리터럴로 둔다)
    private static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";
    private static final String CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    public SignApiControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    // Spring Security의 CookieCsrfTokenRepository는 더블 서브밋 쿠키 방식이라, POST 요청에는
    // 쿠키(XSRF-TOKEN)와 헤더(X-XSRF-TOKEN)에 동일한 CSRF 토큰을 함께 실어야 한다.
    private MockHttpServletRequestBuilder postWithCsrf(String uri) {
        return post(uri)
                .cookie(new Cookie(CSRF_COOKIE_NAME, CSRF_TOKEN))
                .header(CSRF_HEADER_NAME, CSRF_TOKEN);
    }

    @Test
    @DisplayName("계정 인증 요청 성공 시 200 응답 코드를 반환합니다.")
    void signInShouldReturnOk() throws Exception {
        // given
        SignInRequest command = new SignInRequest("testuser", "password");

        // when
        JsonWebToken expectedToken = new JsonWebToken("access-token", "refresh-token", 3600);
        when(authenticationService.authenticate(any(SignInRequest.class))).thenReturn(expectedToken);

        // when & then
        MvcResult result = mockMvc.perform(postWithCsrf("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertThat(setCookieHeaders)
                .anyMatch(cookie -> cookie.startsWith(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME + "=access-token") && cookie.contains("HttpOnly"))
                .anyMatch(cookie -> cookie.startsWith(JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME + "=refresh-token") && cookie.contains("HttpOnly"));
    }

    @Test
    @DisplayName("CSRF 토큰 없이 요청하면 403 응답 코드를 반환합니다.")
    void signInWithoutCsrfTokenShouldReturnForbidden() throws Exception {
        // given
        SignInRequest command = new SignInRequest("testuser", "password");

        // when & then
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CSRF 쿠키와 헤더 값이 다르면 403 응답 코드를 반환합니다.")
    void signInWithMismatchedCsrfTokenShouldReturnForbidden() throws Exception {
        // given
        SignInRequest command = new SignInRequest("testuser", "password");

        // when & then
        mockMvc.perform(post("/api/sign-in")
                        .cookie(new Cookie(CSRF_COOKIE_NAME, CSRF_TOKEN))
                        .header(CSRF_HEADER_NAME, "다른-토큰")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("로그아웃 요청 성공 시 인증 쿠키를 만료시킵니다.")
    void signOutShouldClearAuthCookies() throws Exception {
        // when & then
        MvcResult result = mockMvc.perform(postWithCsrf("/api/sign-out"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andReturn();

        List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
        assertThat(setCookieHeaders)
                .anyMatch(cookie -> cookie.startsWith(JwtAuthenticationFilter.ACCESS_TOKEN_COOKIE_NAME + "=") && cookie.contains("Max-Age=0"))
                .anyMatch(cookie -> cookie.startsWith(JwtAuthenticationFilter.REFRESH_TOKEN_COOKIE_NAME + "=") && cookie.contains("Max-Age=0"));
    }

    @Test
    @DisplayName("인증된 사용자가 본인 정보를 조회하면 200 응답 코드를 반환합니다.")
    @WithMockUser(username = "testuser", authorities = "ROLE_USER")
    void getCurrentUserShouldReturnOk() throws Exception {
        // given
        AuthenticatedUserResponse expected = AuthenticatedUserResponse.builder()
                .userId("user-id")
                .username("testuser")
                .authorities(List.of("ROLE_USER"))
                .build();
        when(authenticationService.getCurrentUser()).thenReturn(expected);

        // when & then
        mockMvc.perform(get("/api/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-id"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 본인 정보를 조회하면 401 응답 코드를 반환합니다.")
    void getCurrentUserShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // when & then
        mockMvc.perform(get("/api/me"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("계정 인증 요청 실패 시 401 응답 코드를 반환합니다.")
    void signInShouldReturnUnauthorized() throws Exception {
        // given
        SignInRequest command = new SignInRequest("testuser", "password");

        when(authenticationService.authenticate(any(SignInRequest.class)))
                .thenThrow(new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // when & then
        mockMvc.perform(postWithCsrf("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("계정 인증 요청 입력값 검증 실패 시 422 응답 코드를 반환합니다.")
    void signInShouldReturnUnprocessableEntity() throws Exception {
        // given
        SignInRequest command = new SignInRequest(null, null);

        // when & then
        mockMvc.perform(postWithCsrf("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("토큰 갱신 요청 성공 시 200 응답 코드를 반환합니다.")
    void refreshTokenShouldReturnOk() throws Exception {
        // given
        TokenRefreshRequest command = new TokenRefreshRequest("valid-refresh-token");
        JsonWebToken expectedToken = new JsonWebToken("new-access-token", "new-refresh-token", 3600);

        when(authenticationService.refreshToken(anyString())).thenReturn(expectedToken);

        // when & then
        mockMvc.perform(postWithCsrf("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("토큰 갱신 요청 실패 시 200 응답 코드를 반환합니다.")
    void refreshTokenShouldReturnUnauthorized() throws Exception {
        // given
        TokenRefreshRequest command = new TokenRefreshRequest("invalid-refresh-token");

        when(authenticationService.refreshToken(anyString()))
                .thenThrow(new BadCredentialsException("유효하지 않은 리프레시 토큰입니다"));

        // when & then
        mockMvc.perform(postWithCsrf("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 요청 입력값 검증 실패 시 422 응답 코드를 반환합니다.")
    void refreshTokenShouldReturnUnprocessableEntity() throws Exception {
        // given
        TokenRefreshRequest command = new TokenRefreshRequest(null);
        // refreshToken 필드를 비워둠

        // when & then
        mockMvc.perform(postWithCsrf("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("네이버 액세스 토큰 검증 성공 시 200 응답 코드를 반환합니다.")
    void authenticateNaverAccessTokenShouldReturnOk() throws Exception {
        // given
        NaverAccessTokenRequest command = new NaverAccessTokenRequest("naver-access-token", "site-id");
        JsonWebToken expectedToken = new JsonWebToken("access-token", "refresh-token", 3600);

        when(naverAccessTokenService.authenticate("naver-access-token", "site-id")).thenReturn(expectedToken);

        // when & then
        mockMvc.perform(postWithCsrf("/api/oauth2/naver/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("네이버 액세스 토큰이 유효하지 않으면 401 응답 코드를 반환합니다.")
    void authenticateNaverAccessTokenShouldReturnUnauthorized() throws Exception {
        // given
        NaverAccessTokenRequest command = new NaverAccessTokenRequest("invalid-token", "site-id");

        when(naverAccessTokenService.authenticate("invalid-token", "site-id"))
                .thenThrow(new BadCredentialsException("네이버 액세스 토큰이 유효하지 않습니다."));

        // when & then
        mockMvc.perform(postWithCsrf("/api/oauth2/naver/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("네이버 액세스 토큰 요청 입력값 검증 실패 시 422 응답 코드를 반환합니다.")
    void authenticateNaverAccessTokenShouldReturnUnprocessableEntity() throws Exception {
        // given
        NaverAccessTokenRequest command = new NaverAccessTokenRequest(null, "site-id");

        // when & then
        mockMvc.perform(postWithCsrf("/api/oauth2/naver/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnprocessableEntity());
    }

}
