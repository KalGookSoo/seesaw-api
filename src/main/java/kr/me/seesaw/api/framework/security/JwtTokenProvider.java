package kr.me.seesaw.api.framework.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kr.me.seesaw.response.JsonWebToken;
import kr.me.seesaw.response.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtTokenProvider {

    private final String secretKey;

    // 액세스 토큰 만료 시간: 1시간
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;

    // 리프레시 토큰 만료 시간: 14일
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 14;

    /**
     * 사용자 정보를 기반으로 액세스 토큰과 리프레시 토큰을 생성합니다.
     *
     * @param userPrincipal 사용자 정보
     * @return 토큰 정보 객체
     */
    public JsonWebToken generateTokenInfo(UserPrincipal userPrincipal) {
        Collection<String> authorities = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String accessToken = generateAccessToken(userPrincipal.getUsername(), userPrincipal.getUser().getId(), authorities);
        String refreshToken = generateRefreshToken(userPrincipal.getUsername());
        return new JsonWebToken(accessToken, refreshToken, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 계정 인증 주체 정보를 암호화한 액세스 토큰을 반환합니다.
     */
    private String generateAccessToken(String username, String userId, Collection<String> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);
        SecretKey secretKey = Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("authorities", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰을 생성합니다.
     */
    private String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);
        SecretKey secretKey = Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .setId(UUID.randomUUID().toString()) // 토큰 ID 설정
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 리프레시 토큰을 검증하고 사용자명을 반환합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 사용자명
     */
    public String validateRefreshToken(String refreshToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            return claims.getSubject();
        } catch (SignatureException e) {
            throw new BadCredentialsException("유효하지 않은 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new BadCredentialsException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("JWT 토큰이 비어있습니다.");
        }
    }

    /**
     * JWT 토큰을 검증하고 인증 객체를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 인증 객체
     */
    public Authentication validateTokenAndGetAuthentication(String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            List<?> authoritiesList = claims.get("authorities", List.class);

            Collection<GrantedAuthority> authorities = authoritiesList.stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.toString()))
                    .collect(Collectors.toList());

            // 인증된 사용자 정보를 담은 Authentication 객체 생성
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        } catch (SignatureException e) {
            throw new BadCredentialsException("유효하지 않은 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            throw new BadCredentialsException("유효하지 않은 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new BadCredentialsException("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("JWT 토큰이 비어있습니다.");
        }
    }

}
