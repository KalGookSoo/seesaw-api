package kr.me.seesaw.security.service;

import kr.me.seesaw.domain.User;
import kr.me.seesaw.repository.UserRepository;
import kr.me.seesaw.security.JwtTokenProvider;
import kr.me.seesaw.security.dto.SignInRequest;
import kr.me.seesaw.security.dto.SignInResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SignInResponse authenticate(SignInRequest request) {
        // 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // 패스워드 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다");
        }

        // 계정 상태 확인
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("만료된 계정입니다");
        }

        if (!user.isAccountNonLocked()) {
            throw new LockedException("잠긴 계정입니다");
        }

        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("패스워드가 만료되었습니다");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new SignInResponse(token);
    }
}
