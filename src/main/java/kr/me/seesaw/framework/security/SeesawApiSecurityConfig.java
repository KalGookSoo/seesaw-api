package kr.me.seesaw.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.me.seesaw.framework.context.properties.SeesawApiProperties;
import kr.me.seesaw.framework.web.filter.CsrfCookieFilter;
import kr.me.seesaw.framework.web.filter.JwtAuthenticationFilter;
import kr.me.seesaw.api.role.RoleService;
import kr.me.seesaw.core.support.audit.IpAddressExtractor;
import kr.me.seesaw.core.support.audit.ServletIpAddressExtractor;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.core.support.authentication.SecurityPrincipalProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SeesawApiSecurityConfig {

    private final SeesawApiProperties seesawApiProperties;

    @Bean
    public PermissionEvaluator seesawPermissionEvaluator(RoleService roleService) {
        return new SeesawPermissionEvaluator(roleService);
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(@Value("${jwt.secret.key}") String secretKey) {
        return new JwtTokenProvider(secretKey);
    }

    @Bean
    public PrincipalProvider principalProvider() {
        return new SecurityPrincipalProvider();
    }

    @Bean
    public IpAddressExtractor ipAddressExtractor() {
        return new ServletIpAddressExtractor();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder
                .json()
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RequestMappingHandlerMapping requestMappingHandlerMapping, ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(jwtTokenProvider, requestMappingHandlerMapping, objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증을 httpOnly 쿠키(SEESAW_ACCESS_TOKEN)로도 받을 수 있게 되어, SameSite=None 쿠키가 진짜
    // 크로스 사이트 요청에도 자동으로 실려가는 것을 막기 위해 더블 서브밋 쿠키 방식의 CSRF 방어가 필요해졌다.
    // CookieCsrfTokenRepository의 기본 네이밍(XSRF-TOKEN 쿠키, X-XSRF-TOKEN 헤더)을 그대로 사용한다.
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.secure(true).sameSite("None").path("/"));
        return repository;
    }

    @Bean
    public CsrfCookieFilter csrfCookieFilter() {
        return new CsrfCookieFilter();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) -> {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "CSRF 토큰이 유효하지 않거나 접근 권한이 없습니다.")));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CsrfCookieFilter csrfCookieFilter,
            CsrfTokenRepository csrfTokenRepository,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
                // CookieCsrfTokenRepository는 쿠키에 원문 토큰을 저장하므로, BREACH 방어용으로 헤더 값을
                // XOR 마스킹해서 비교하는 기본 핸들러 대신 원문 그대로 비교하는 핸들러를 써야 한다.
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        );
        http.exceptionHandling(handler -> handler.accessDeniedHandler(accessDeniedHandler));
        http.cors(this::handleCorsPolicies);
        http.authorizeHttpRequests(this::handleAuthorizeHttpRequests);
        http.sessionManagement(this::handleSeesionManagement);

        // 매 요청마다 CsrfToken을 강제로 읾어 쿠키가 항상 발급되도록 한다(지연 로딩 우회)
        http.addFilterAfter(csrfCookieFilter, BasicAuthenticationFilter.class);
        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleCorsPolicies(CorsConfigurer<HttpSecurity> config) {
        config.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(seesawApiProperties.getCorsAllowedOrigins());
            configuration.setAllowedMethods(Arrays.asList(
                    HttpMethod.GET.name(),
                    HttpMethod.POST.name(),
                    HttpMethod.PUT.name(),
                    HttpMethod.DELETE.name(),
                    HttpMethod.PATCH.name(),
                    HttpMethod.OPTIONS.name()
            ));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setExposedHeaders(Arrays.asList(
                    HttpHeaders.AUTHORIZATION,
                    HttpHeaders.CONTENT_TYPE
            ));

            // 인증 정보 허용
            configuration.setAllowCredentials(true);
            // Preflight 요청 캐시 시간 (초)
            configuration.setMaxAge(3600L);
            return configuration;
        });
    }

    private void handleAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry config) {
        config.requestMatchers(new AntPathRequestMatcher("/favicon.png")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/info")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).hasRole("ADMIN")
                .anyRequest()
                .permitAll();
    }

    private void handleSeesionManagement(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

}
