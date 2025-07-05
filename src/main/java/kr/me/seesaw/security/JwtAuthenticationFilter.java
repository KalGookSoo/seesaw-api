package kr.me.seesaw.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.me.seesaw.core.authentication.PrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final PrincipalProvider principalProvider;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // PrincipalProvider를 통해 인증 객체 가져오기
            Authentication authentication = principalProvider.getAuthentication();

            // 인증 객체가 존재하면 SecurityContext에 설정
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 인증 처리 중 예외가 발생해도 요청은 계속 처리되도록 함
            // 로그만 남기고 예외는 전파하지 않음
            logger.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        try {
            HandlerExecutionChain handlerExecutionChain = requestMappingHandlerMapping.getHandler(request);
            if (handlerExecutionChain == null) {
                return true;
            }
            Object handler = handlerExecutionChain.getHandler();
            if (handler instanceof HandlerMethod handlerMethod) {
                Method method = handlerMethod.getMethod();
                boolean hasPreAuthorize = method.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class);
                boolean hasSecured = method.isAnnotationPresent(org.springframework.security.access.annotation.Secured.class);
                return !(hasPreAuthorize || hasSecured);
            }
        } catch (Exception e) {
            return true;
        }
        return true;

    }
}
