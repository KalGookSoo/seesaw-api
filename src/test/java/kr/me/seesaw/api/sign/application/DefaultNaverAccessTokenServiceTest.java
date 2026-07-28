package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.sign.AuthenticationService;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNaverAccessTokenServiceTest {

    @Mock
    private NaverProfileClient naverProfileClient;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SiteRepository siteRepository;

    @Test
    void authenticateShouldCreateUserAssignRoleAndIssueToken() {
        DefaultNaverAccessTokenService service = createService();
        NaverUserProfile profile = new NaverUserProfile("naver-id", "tester@naver.com", "테스터");
        Site site = new Site();
        site.setId("site-id");
        Role role = new Role();
        role.setName(RoleName.ROLE_USER.name());
        JsonWebToken token = new JsonWebToken("access-token", "refresh-token", 3600);

        when(siteRepository.findById("site-id")).thenReturn(Optional.of(site));
        when(naverProfileClient.getProfile("naver-access-token")).thenReturn(profile);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleName.ROLE_USER.name())).thenReturn(Optional.of(role));
        when(authenticationService.issueToken("tester", "site-id")).thenReturn(token);

        JsonWebToken result = service.authenticate("naver-access-token", "site-id");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("tester");
        assertThat(savedUser.getEmail().toString()).isEqualTo("tester@naver.com");
        assertThat(savedUser.getRoleMappings()).hasSize(1);
        assertThat(savedUser.getRoleMappings().get(0).getSite()).isEqualTo(site);
        assertThat(savedUser.getRoleMappings().get(0).getRole()).isEqualTo(role);
        assertThat(result).isEqualTo(token);
    }

    @Test
    void authenticateShouldNotDuplicateRoleWhenAlreadyAssignedForSite() {
        DefaultNaverAccessTokenService service = createService();
        Site site = new Site();
        site.setId("site-id");
        Role role = new Role();
        role.setName(RoleName.ROLE_USER.name());

        User existingUser = new User();
        existingUser.setUsername("tester");
        existingUser.setEmail(new Email("tester", "naver.com"));
        RoleMapping existingMapping = new RoleMapping();
        existingMapping.setSite(site);
        existingMapping.setRole(role);
        existingUser.addRole(existingMapping);

        JsonWebToken token = new JsonWebToken("access-token", "refresh-token", 3600);

        when(siteRepository.findById("site-id")).thenReturn(Optional.of(site));
        when(naverProfileClient.getProfile("naver-access-token"))
                .thenReturn(new NaverUserProfile("naver-id", "tester@naver.com", "테스터"));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(existingUser));
        when(authenticationService.issueToken("tester", "site-id")).thenReturn(token);

        service.authenticate("naver-access-token", "site-id");

        assertThat(existingUser.getRoleMappings()).hasSize(1);
        verifyNoInteractions(roleRepository);
    }

    @Test
    void authenticateShouldThrowWhenSiteNotFound() {
        DefaultNaverAccessTokenService service = createService();
        when(siteRepository.findById("missing-site")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("naver-access-token", "missing-site"))
                .isInstanceOf(NoSuchElementException.class);
        verifyNoInteractions(naverProfileClient);
    }

    @Test
    void authenticateShouldRejectMissingAccessToken() {
        DefaultNaverAccessTokenService service = createService();

        assertThatThrownBy(() -> service.authenticate(null, "site-id"))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(siteRepository, naverProfileClient);
    }

    @Test
    void authenticateShouldRejectExistingUsernameWithDifferentEmail() {
        DefaultNaverAccessTokenService service = createService();
        Site site = new Site();
        site.setId("site-id");
        User existingUser = new User();
        existingUser.setUsername("tester");
        existingUser.setEmail(new Email("other", "example.com"));

        when(siteRepository.findById("site-id")).thenReturn(Optional.of(site));
        when(naverProfileClient.getProfile("naver-access-token"))
                .thenReturn(new NaverUserProfile("naver-id", "tester@naver.com", "테스터"));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> service.authenticate("naver-access-token", "site-id"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일이 일치하지 않습니다");
        verifyNoInteractions(authenticationService);
    }

    @Test
    void authenticateShouldRejectMalformedEmail() {
        DefaultNaverAccessTokenService service = createService();
        Site site = new Site();
        site.setId("site-id");

        when(siteRepository.findById("site-id")).thenReturn(Optional.of(site));
        when(naverProfileClient.getProfile("naver-access-token"))
                .thenReturn(new NaverUserProfile("naver-id", "not-an-email", "테스터"));

        assertThatThrownBy(() -> service.authenticate("naver-access-token", "site-id"))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(userRepository);
    }

    private DefaultNaverAccessTokenService createService() {
        return new DefaultNaverAccessTokenService(
                naverProfileClient,
                authenticationService,
                userRepository,
                roleRepository,
                siteRepository
        );
    }

}
