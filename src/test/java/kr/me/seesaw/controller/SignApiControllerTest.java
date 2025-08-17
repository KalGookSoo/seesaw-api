package kr.me.seesaw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.me.seesaw.command.SignInCommand;
import kr.me.seesaw.command.TokenRefreshCommand;
import kr.me.seesaw.model.JsonWebToken;
import kr.me.seesaw.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SignApiControllerTest {

    /**
     * AuthenticationService의 모의 객체를 생성하여 컨트롤러에 주입합니다.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public AuthenticationService authenticationService() {
            return Mockito.mock(AuthenticationService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("로그인 요청이 성공하면 JWT 토큰을 반환한다")
    void signIn_Success_ReturnsJwtToken() throws Exception {
        // given - 테스트 데이터 설정
        SignInCommand command = new SignInCommand();
        command.setUsername("testuser");
        command.setPassword("password");

        // 모의 서비스가 반환할 토큰 설정
        JsonWebToken expectedToken = new JsonWebToken("access-token", "refresh-token", 3600);
        when(authenticationService.authenticate(any(SignInCommand.class))).thenReturn(expectedToken);

        // when & then - API 호출 및 결과 검증
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("로그인 요청 시 유효하지 않은 자격 증명이면 401 오류를 반환한다")
    void signIn_InvalidCredentials_Returns401() throws Exception {
        // given
        SignInCommand command = new SignInCommand();
        command.setUsername("testuser");
        command.setPassword("wrong-password");

        when(authenticationService.authenticate(any(SignInCommand.class)))
                .thenThrow(new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // when & then
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 요청 시 필수 필드가 누락되면 422 오류를 반환한다")
    void signIn_MissingRequiredFields_Returns400() throws Exception {
        // given
        SignInCommand command = new SignInCommand();
        // username과 password 필드를 비워둠

        // when & then
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("토큰 갱신 요청이 성공하면 새로운 JWT 토큰을 반환한다")
    void refreshToken_Success_ReturnsNewJwtToken() throws Exception {
        // given
        TokenRefreshCommand command = new TokenRefreshCommand("valid-refresh-token");
        JsonWebToken expectedToken = new JsonWebToken("new-access-token", "new-refresh-token", 3600);

        when(authenticationService.refreshToken(anyString())).thenReturn(expectedToken);

        // when & then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("토큰 갱신 요청 시 유효하지 않은 리프레시 토큰이면 401 오류를 반환한다")
    void refreshToken_InvalidRefreshToken_Returns401() throws Exception {
        // given
        TokenRefreshCommand command = new TokenRefreshCommand("invalid-refresh-token");

        when(authenticationService.refreshToken(anyString()))
                .thenThrow(new BadCredentialsException("유효하지 않은 리프레시 토큰입니다"));

        // when & then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 요청 시 리프레시 토큰이 누락되면 422 오류를 반환한다")
    void refreshToken_MissingRefreshToken_Returns422() throws Exception {
        // given
        TokenRefreshCommand command = new TokenRefreshCommand();
        // refreshToken 필드를 비워둠

        // when & then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnprocessableEntity());
    }
}
