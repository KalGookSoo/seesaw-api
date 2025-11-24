package kr.me.seesaw.service;

import kr.me.seesaw.command.SignInCommand;
import kr.me.seesaw.domain.Role;
import kr.me.seesaw.domain.RoleMapping;
import kr.me.seesaw.domain.User;
import kr.me.seesaw.model.JsonWebToken;
import kr.me.seesaw.model.RoleModel;
import kr.me.seesaw.model.UserModel;
import kr.me.seesaw.model.UserPrincipal;
import kr.me.seesaw.repository.RoleRepository;
import kr.me.seesaw.repository.UserRepository;
import kr.me.seesaw.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 로그인을 처리합니다.
     *
     * @param command 로그인 정보
     * @return JsonWebToken
     */
    @Transactional(readOnly = true)
    @Override
    public JsonWebToken authenticate(SignInCommand command) {
        // 사용자 조회
        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // 패스워드 검증
        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
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
        String username = jwtTokenProvider.validateRefreshToken(refreshToken);
        UserPrincipal userPrincipal = loadUserPrincipal(username);
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    /**
     * 사용자 정보를 기반으로 UserPrincipal을 생성합니다.
     *
     * @param username 사용자명
     * @return UserPrincipal
     */
    private UserPrincipal loadUserPrincipal(String username) {
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
        List<String> roleIds = user.getRoleMappings()
                .stream()
                .map(RoleMapping::getRole)
                .map(Role::getId)
                .toList();
        List<Role> roles = roleRepository.findAllByIdIn(roleIds);
        Set<RoleModel> roleModels = roles.stream()
                .map(RoleModel::new)
                .collect(Collectors.toSet());

        UserModel userModel = new UserModel(user);
        roleModels.forEach(userModel::addRole);

        return new UserPrincipal(userModel);
    }

}
