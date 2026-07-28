package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.security.dto.JsonWebToken;
import kr.me.seesaw.api.sign.AuthenticationService;
import kr.me.seesaw.api.sign.NaverProfileClient;
import kr.me.seesaw.api.sign.dto.NaverUserProfile;
import kr.me.seesaw.core.domain.user.Email;
import kr.me.seesaw.core.domain.user.User;
import kr.me.seesaw.core.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

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

    @Test
    void authenticateShouldCreateUserAndIssueToken() {
        DefaultNaverAccessTokenService service = createService();
        NaverUserProfile profile = new NaverUserProfile("naver-id", "tester@naver.com", "테스터");
        JsonWebToken token = new JsonWebToken("access-token", "refresh-token", 3600);

        when(naverProfileClient.getProfile("naver-access-token")).thenReturn(profile);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.empty());
        when(authenticationService.issueToken("tester")).thenReturn(token);

        JsonWebToken result = service.authenticate("naver-access-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("tester");
        assertThat(userCaptor.getValue().getEmail().toString()).isEqualTo("tester@naver.com");
        assertThat(result).isEqualTo(token);
    }

    @Test
    void authenticateShouldReuseExistingUserWithMatchingEmail() {
        DefaultNaverAccessTokenService service = createService();
        NaverUserProfile profile = new NaverUserProfile("naver-id", "tester@naver.com", "테스터");
        User existingUser = new User();
        existingUser.setUsername("tester");
        existingUser.setEmail(new Email("tester", "naver.com"));
        JsonWebToken token = new JsonWebToken("access-token", "refresh-token", 3600);

        when(naverProfileClient.getProfile("naver-access-token")).thenReturn(profile);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(existingUser));
        when(authenticationService.issueToken("tester")).thenReturn(token);

        JsonWebToken result = service.authenticate("naver-access-token");

        verify(userRepository).save(existingUser);
        assertThat(result).isEqualTo(token);
    }

    @Test
    void authenticateShouldRejectMissingAccessToken() {
        DefaultNaverAccessTokenService service = createService();

        assertThatThrownBy(() -> service.authenticate(null))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(naverProfileClient);
    }

    @Test
    void authenticateShouldRejectExistingUsernameWithDifferentEmail() {
        DefaultNaverAccessTokenService service = createService();
        User existingUser = new User();
        existingUser.setUsername("tester");
        existingUser.setEmail(new Email("other", "example.com"));

        when(naverProfileClient.getProfile("naver-access-token"))
                .thenReturn(new NaverUserProfile("naver-id", "tester@naver.com", "테스터"));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> service.authenticate("naver-access-token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("이메일이 일치하지 않습니다");
        verifyNoInteractions(authenticationService);
    }

    @Test
    void authenticateShouldRejectMalformedEmail() {
        DefaultNaverAccessTokenService service = createService();

        when(naverProfileClient.getProfile("naver-access-token"))
                .thenReturn(new NaverUserProfile("naver-id", "not-an-email", "테스터"));

        assertThatThrownBy(() -> service.authenticate("naver-access-token"))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(userRepository);
    }

    private DefaultNaverAccessTokenService createService() {
        return new DefaultNaverAccessTokenService(naverProfileClient, authenticationService, userRepository);
    }

}
