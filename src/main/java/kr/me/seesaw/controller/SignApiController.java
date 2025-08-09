package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.SignInCommand;
import kr.me.seesaw.command.TokenRefreshCommand;
import kr.me.seesaw.model.JsonWebToken;
import kr.me.seesaw.service.AuthenticationService;
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

    @PostMapping("/sign-in")
    public ResponseEntity<JsonWebToken> signIn(@Valid @RequestBody SignInCommand command) {
        JsonWebToken response = authenticationService.authenticate(command);
        return ResponseEntity.ok(response);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     * @param command 토큰 갱신 요청
     * @return 새로운 JWT 토큰이 포함된 응답
     */
    @PostMapping("/token/refresh")
    public ResponseEntity<JsonWebToken> refreshToken(@Valid @RequestBody TokenRefreshCommand command) {
        JsonWebToken response = authenticationService.refreshToken(command.getRefreshToken());
        return ResponseEntity.ok(response);
    }


}
