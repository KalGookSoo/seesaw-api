package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.api.sign.NaverAccessTokenService;
import kr.me.seesaw.api.sign.NaverProfileClient;
import kr.me.seesaw.api.sign.dto.NaverUserProfile;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.core.domain.role.RoleRepository;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.site.SiteRepository;
import kr.me.seesaw.core.domain.user.Email;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;

@Service
@Transactional
public class DefaultNaverAccessTokenService implements NaverAccessTokenService {

    private final NaverProfileClient naverProfileClient;

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final SiteRepository siteRepository;

    public DefaultNaverAccessTokenService(
            NaverProfileClient naverProfileClient,
            AuthenticationService authenticationService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            SiteRepository siteRepository
    ) {
        this.naverProfileClient = naverProfileClient;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.siteRepository = siteRepository;
    }

    @Override
    public JsonWebToken authenticate(String accessToken, String siteId) {
        if (!StringUtils.hasText(accessToken)) {
            throw new BadCredentialsException("네이버 액세스 토큰이 없습니다.");
        }
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new NoSuchElementException("사이트를 찾을 수 없습니다."));

        NaverUserProfile profile = naverProfileClient.getProfile(accessToken);
        User user = findOrCreateUser(profile);
        assignUserRole(user, site);
        userRepository.save(user);
        return authenticationService.issueToken(user.getUsername(), siteId);
    }

    private User findOrCreateUser(NaverUserProfile profile) {
        String[] emailParts = profile.email().split("@", 2);
        if (emailParts.length != 2) {
            throw new BadCredentialsException("네이버 계정 이메일 형식이 올바르지 않습니다.");
        }
        String username = emailParts[0];
        return userRepository.findByUsername(username)
                .map(user -> validateEmail(user, profile.email()))
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setName(profile.name());
                    user.setEmail(new Email(emailParts[0], emailParts[1]));
                    user.initializeAccountPolicy();
                    return user;
                });
    }

    private User validateEmail(User user, String email) {
        if (!email.equalsIgnoreCase(user.getEmail().toString())) {
            throw new BadCredentialsException("동일한 사용자명의 기존 계정과 네이버 이메일이 일치하지 않습니다.");
        }
        return user;
    }

    private void assignUserRole(User user, Site site) {
        boolean mapped = user.getRoleMappings()
                .stream()
                .anyMatch(mapping -> mapping.getRole() != null
                        && mapping.getSite() != null
                        && mapping.getRole().has(RoleName.ROLE_USER)
                        && site.getId().equals(mapping.getSite().getId()));
        if (mapped) {
            return;
        }

        Role role = roleRepository.findByName(RoleName.ROLE_USER.name())
                .orElseThrow(() -> new NoSuchElementException("일반사용자 역할이 없습니다."));
        RoleMapping roleMapping = new RoleMapping();
        roleMapping.setRole(role);
        roleMapping.setSite(site);
        user.addRole(roleMapping);
    }

}
