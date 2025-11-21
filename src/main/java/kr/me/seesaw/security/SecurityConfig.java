package kr.me.seesaw.security;

import kr.me.seesaw.core.authentication.PrincipalProvider;
import kr.me.seesaw.core.authentication.SecurityPrincipalProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(@Value("${jwt.secret.key}") String secretKey) {
        return new JwtTokenProvider(secretKey);
    }

    @Bean
    public PrincipalProvider principalProvider() {
        return new SecurityPrincipalProvider();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        return new JwtAuthenticationFilter(jwtTokenProvider, requestMappingHandlerMapping);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(this::handleCorsPolicies);
        http.authorizeHttpRequests(this::handleAuthorizeHttpRequests);
        http.sessionManagement(this::handleSeesionManagement);

        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleCorsPolicies(CorsConfigurer<HttpSecurity> config) {
        config.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(Arrays.asList(
                    "http://localhost:*",
                    "http://127.0.0.1:*",
                    "https://*.seesaw.me.kr"
            ));
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
        config.requestMatchers(new AntPathRequestMatcher("/favicon.ico")).permitAll()
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
