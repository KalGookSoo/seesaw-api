package kr.me.seesaw.security;

import jakarta.servlet.http.HttpServletRequest;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * HTTP 요청 헤더에서 JWT 토큰을 추출하여 인증 객체를 제공하는 구현체
 */
@RequiredArgsConstructor
public class HeaderPrincipalProvider implements PrincipalProvider {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Authentication getAuthentication() {
        // 현재 요청에서 Authorization 헤더 추출
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Bearer 토큰이 존재하는 경우 처리
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 추출
            try {
                // JwtTokenProvider를 사용하여 토큰 검증 및 인증 객체 반환
                return jwtTokenProvider.validateTokenAndGetAuthentication(token);
            } catch (Exception e) {
                // 토큰 검증 실패 시 로그 기록
                logger.error("JWT 토큰 검증 실패: {}", e.getMessage());
                throw new BadCredentialsException("계정 인증에 실패했습니다.");
            }
        }
        // 토큰이 없거나 유효하지 않은 경우 null 반환 (인증되지 않은 요청으로 처리)
        return null;
    }
}
