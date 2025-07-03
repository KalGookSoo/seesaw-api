package kr.me.seesaw.controller;

import jakarta.validation.Valid;
import kr.me.seesaw.command.SignInCommand;
import kr.me.seesaw.model.JsonWebToken;
import kr.me.seesaw.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SignApiController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-in")
    public ResponseEntity<JsonWebToken> signIn(@Valid @RequestBody SignInCommand request) {
        JsonWebToken response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }


}
