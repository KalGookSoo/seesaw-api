package kr.me.seesaw.security;

import kr.me.seesaw.core.authentication.PrincipalProvider;
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
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final String secretKey;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public SecurityConfig(@Value("${jwt.secret.key}") String secretKey, RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.secretKey = secretKey;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(secretKey);
    }

    @Bean
    public PrincipalProvider principalProvider() {
        return new HeaderPrincipalProvider(jwtTokenProvider());
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(principalProvider(), requestMappingHandlerMapping);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(this::handleCorsPolicies);
        http.authorizeHttpRequests(this::handleAuthorizeHttpRequests);
        http.sessionManagement(this::handleSeesionManagement);

        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleCorsPolicies(CorsConfigurer<HttpSecurity> config) {
        config.configurationSource(request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOriginPatterns(Collections.singletonList("/**"));
            configuration.setAllowedOrigins(Collections.singletonList(request.getHeader(HttpHeaders.ORIGIN)));
            configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
            configuration.setAllowedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CACHE_CONTROL, HttpHeaders.CONTENT_TYPE));
            configuration.setAllowCredentials(true);
            return configuration;
        });
    }

    private void handleAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry config) {
        config.requestMatchers(new AntPathRequestMatcher("/favicon.ico")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).hasRole("ADMIN")
                .anyRequest()
                .permitAll();
    }

    private void handleSeesionManagement(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

}
