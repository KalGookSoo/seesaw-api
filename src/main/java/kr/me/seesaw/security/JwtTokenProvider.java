package kr.me.seesaw.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.me.seesaw.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

@RequiredArgsConstructor
public class JwtTokenProvider {

    private final String secretKey;

    private static final long EXPIRATION = 1000 * 60 * 60;

    public String generateToken(UserPrincipal userPrincipal) {

        Collection<String> authorities = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return generateToken(userPrincipal.getUsername(), authorities);
    }

    private String generateToken(String username, Collection<String> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        SecretKey secretKey = Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

}
