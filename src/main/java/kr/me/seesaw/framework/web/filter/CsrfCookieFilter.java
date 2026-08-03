package kr.me.seesaw.framework.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6의 CsrfToken은 요청 시점에 실제로 접근하기 전까지는(BREACH 공격 대응을 위해)
 * 지연 로딩되어, 그 접근이 없으면 CookieCsrfTokenRepository가 쿠키를 내려주지 않는다. 매 요청마다
 * 토큰 값을 강제로 읾어 항상 쿠키가 발급되도록 한다(Spring Security 공식 SPA 연동 가이드의 패턴).
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }

}
