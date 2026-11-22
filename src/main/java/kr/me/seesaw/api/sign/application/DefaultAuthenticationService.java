package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.sign.dto.SignInRequest;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.api.security.dto.AuthenticatedUserResponse;
import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.role.dto.RoleResponse;
import kr.me.seesaw.api.user.dto.UserResponse;
import kr.me.seesaw.framework.security.UserPrincipal;
import kr.me.seesaw.core.domain.role.RoleRepository;
import kr.me.seesaw.core.domain.user.UserRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.framework.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final PrincipalProvider principalProvider;

    /**
     * 사용자 로그인을 처리합니다.
     *
     * @param command 로그인 정보
     * @return JsonWebToken
     */
    @Transactional(readOnly = true)
    @Override
    public JsonWebToken authenticate(SignInRequest command) {
        logger.info("사용자 인증 요청: username={}", command.username());
        // 사용자 조회
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // 패스워드 검증
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
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

        // 액세스 토큰과 리프레시 토큰 생성
        UserPrincipal userPrincipal = createUserPrincipal(user);
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 재발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return JsonWebToken
     */
    @Transactional(readOnly = true)
    @Override
    public JsonWebToken refreshToken(String refreshToken) {
        logger.info("토큰 재발급 요청");
        String username = jwtTokenProvider.validateRefreshToken(refreshToken);
        UserPrincipal userPrincipal = loadUserPrincipal(username);
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    /**
     * 이미 신원이 확인된 사용자명으로 JWT 토큰을 발급합니다.
     *
     * @param username 사용자명
     * @return JsonWebToken
     */
    @Transactional(readOnly = true)
    @Override
    public JsonWebToken issueToken(String username) {
        logger.info("토큰 발급 요청: username={}", username);
        UserPrincipal userPrincipal = loadUserPrincipal(username);
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    /**
     * 이미 신원이 확인된 사용자명으로, 특정 사이트에서 행사할 수 있는 역할만 담아 JWT 토큰을 발급합니다.
     *
     * @param username 사용자명
     * @param siteId   사이트 식별자
     * @return JsonWebToken
     */
    @Transactional(readOnly = true)
    @Override
    public JsonWebToken issueToken(String username, String siteId) {
        logger.info("토큰 발급 요청: username={}, siteId={}", username, siteId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));
        UserPrincipal userPrincipal = createUserPrincipal(user, siteId);
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    /**
     * 현재 인증된 사용자 본인의 정보를 조회합니다.
     *
     * @return 계정 식별자/사용자명/권한이 담긴 응답
     */
    @Transactional(readOnly = true)
    @Override
    public AuthenticatedUserResponse getCurrentUser() {
        String username = principalProvider.getAuthentication().getName();
        logger.info("현재 사용자 조회: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("해당 계정이 존재하지 않습니다."));

        List<String> authorities = principalProvider.getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return AuthenticatedUserResponse.builder()
                .userId(user.getId())
                .username(username)
                .email(user.getEmail().toString())
                .authorities(authorities)
                .build();
    }

    /**
     * 사용자 정보를 기반으로 UserPrincipal을 생성합니다.
     *
     * @param username 사용자명
     * @return UserPrincipal
     */
    private UserPrincipal loadUserPrincipal(String username) {
        logger.info("사용자 Principal 로드: username={}", username);
        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        return createUserPrincipal(user);
    }

    /**
     * 사용자 정보를 기반으로 UserPrincipal을 생성합니다.
     *
     * @param user 사용자 정보
     * @return UserPrincipal
     */
    private UserPrincipal createUserPrincipal(User user) {
        logger.info("사용자 Principal 생성: username={}", user.getUsername());
        List<String> roleIds = user.getRoleMappings()
                .stream()
                .map(RoleMapping::getRole)
                .map(Role::getId)
                .toList();
        List<Role> roles = roleRepository.findAllByIdIn(roleIds);
        Set<RoleResponse> roleModels = roles.stream()
                .map(RoleResponse::new)
                .collect(Collectors.toSet());

        UserResponse userModel = new UserResponse(user);
        roleModels.forEach(userModel::addRole);

        return new UserPrincipal(userModel);
    }

    /**
     * 사용자 정보를 기반으로, 특정 사이트에서 행사할 수 있는 역할만 담은 UserPrincipal을 생성합니다.
     *
     * @param user   사용자 정보
     * @param siteId 사이트 식별자
     * @return UserPrincipal
     */
    private UserPrincipal createUserPrincipal(User user, String siteId) {
        logger.info("사용자 Principal 생성: username={}, siteId={}", user.getUsername(), siteId);
        List<String> roleIds = user.getRoleMappings()
                .stream()
                .filter(mapping -> mapping.getSite() != null && siteId.equals(mapping.getSite().getId()))
                .map(RoleMapping::getRole)
                .map(Role::getId)
                .toList();
        List<Role> roles = roleRepository.findAllByIdIn(roleIds);
        Set<RoleResponse> roleModels = roles.stream()
                .map(RoleResponse::new)
                .collect(Collectors.toSet());

        UserResponse userModel = new UserResponse(user);
        roleModels.forEach(userModel::addRole);

        return new UserPrincipal(userModel);
    }

}
