package kr.me.seesaw.api.framework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.me.seesaw.request.SignInRequest;
import kr.me.seesaw.request.TokenRefreshRequest;
import kr.me.seesaw.response.JsonWebToken;
import kr.me.seesaw.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    public SignApiControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
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
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("계정 인증 요청 실패 시 401 응답 코드를 반환합니다.")
    void signInShouldReturnUnauthorized() throws Exception {
        // given
        SignInRequest command = new SignInRequest("testuser", "password");

        when(authenticationService.authenticate(any(SignInRequest.class)))
                .thenThrow(new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // when & then
        mockMvc.perform(post("/api/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("계정 인증 요청 입력값 검증 실패 시 422 응답 코드를 반환합니다.")
    void signInShouldReturnUnprocessableEntity() throws Exception {
        // given
        SignInRequest command = new SignInRequest();

        // when & then
        mockMvc.perform(post("/api/sign-in")
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
        mockMvc.perform(post("/api/token/refresh")
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
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 갱신 요청 입력값 검증 실패 시 422 응답 코드를 반환합니다.")
    void refreshTokenShouldReturnUnprocessableEntity() throws Exception {
        // given
        TokenRefreshRequest command = new TokenRefreshRequest();
        // refreshToken 필드를 비워둠

        // when & then
        mockMvc.perform(post("/api/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isUnprocessableEntity());
    }

}
