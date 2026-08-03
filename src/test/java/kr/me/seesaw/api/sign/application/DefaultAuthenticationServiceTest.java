package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.core.domain.mapping.RoleMapping;
import kr.me.seesaw.core.domain.role.Role;
import kr.me.seesaw.core.domain.role.RoleName;
import kr.me.seesaw.core.domain.role.RoleRepository;
import kr.me.seesaw.core.domain.site.Site;
import kr.me.seesaw.core.domain.user.Email;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import kr.me.seesaw.core.support.authentication.PrincipalProvider;
import kr.me.seesaw.framework.security.JwtTokenProvider;
import kr.me.seesaw.framework.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PrincipalProvider principalProvider;

    @Test
    void issueTokenWithSiteIdShouldOnlyIncludeRoleForThatSite() {
        DefaultAuthenticationService service = createService();

        Site siteA = new Site();
        siteA.setId("site-a");
        Site siteB = new Site();
        siteB.setId("site-b");

        Role roleForSiteA = new Role();
        roleForSiteA.setId("role-a");
        roleForSiteA.setName(RoleName.ROLE_USER.name());
        Role roleForSiteB = new Role();
        roleForSiteB.setId("role-b");
        roleForSiteB.setName(RoleName.ROLE_MANAGER.name());

        RoleMapping mappingA = new RoleMapping();
        mappingA.setSite(siteA);
        mappingA.setRole(roleForSiteA);
        RoleMapping mappingB = new RoleMapping();
        mappingB.setSite(siteB);
        mappingB.setRole(roleForSiteB);

        User user = new User();
        user.setUsername("tester");
        user.setEmail(new Email("tester", "example.com"));
        user.addRole(mappingA);
        user.addRole(mappingB);

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(roleRepository.findAllByIdIn(List.of("role-a"))).thenReturn(List.of(roleForSiteA));
        when(jwtTokenProvider.generateTokenInfo(any(UserPrincipal.class)))
                .thenReturn(new JsonWebToken("access-token", "refresh-token", 3600));

        JsonWebToken result = service.issueToken("tester", "site-a");

        ArgumentCaptor<UserPrincipal> principalCaptor = ArgumentCaptor.forClass(UserPrincipal.class);
        verify(jwtTokenProvider).generateTokenInfo(principalCaptor.capture());
        assertThat(principalCaptor.getValue().getAuthorities())
                .extracting("authority")
                .containsExactly(RoleName.ROLE_USER.name());
        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    void issueTokenWithSiteIdShouldRejectUnknownUsername() {
        DefaultAuthenticationService service = createService();
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issueToken("unknown", "site-a"))
                .isInstanceOf(BadCredentialsException.class);
    }

    private DefaultAuthenticationService createService() {
        return new DefaultAuthenticationService(userRepository, roleRepository, jwtTokenProvider, passwordEncoder, principalProvider);
    }

}
