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

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultAuthenticationService implements AuthenticationService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public JsonWebToken authenticate(SignInCommand command) {
        // 사용자 조회
        User user = userRepository.findByUsername(command.getUsername())
                .orElseThrow(() -> new BadCredentialsException("사용자명 또는 패스워드가 일치하지 않습니다"));

        // 사용자가 가진 역할 조회
        List<RoleMapping> mappings = user.getRoleMappings();
        List<String> roleIds = mappings.stream()
                .map(RoleMapping::getRole)
                .map(Role::getId)
                .toList();

        // 사용자가 가진 역할을 모델에 할당
        List<Role> roles = new LinkedHashSet<>(roleRepository.findAllByIdIn(roleIds)).stream().toList();
        UserModel userModel = new UserModel(user);
        roles.stream().map(RoleModel::new).forEach(userModel::addRole);

        UserPrincipal userPrincipal = new UserPrincipal(userModel);

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
        return jwtTokenProvider.generateTokenInfo(userPrincipal);
    }

    @Override
    public JsonWebToken refreshToken(String refreshToken) {
        return jwtTokenProvider.refreshToken(refreshToken);
    }

}
