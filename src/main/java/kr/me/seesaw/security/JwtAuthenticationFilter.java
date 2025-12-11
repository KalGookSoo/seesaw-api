package kr.me.seesaw.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Bearer 토큰이 존재하는 경우 처리
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 추출
            try {
                Authentication authentication = jwtTokenProvider.validateTokenAndGetAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException e) {
                SecurityContextHolder.clearContext();
                if (!response.isCommitted()) {
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    String body = objectMapper.writeValueAsString(Map.of("message", e.getMessage()));
                    response.getWriter().write(body);
                }
                return; // 필터체인 중단
            }
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
                boolean hasPreAuthorize = method.isAnnotationPresent(PreAuthorize.class);
                boolean hasSecured = method.isAnnotationPresent(Secured.class);
                return !(hasPreAuthorize || hasSecured);
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

}
