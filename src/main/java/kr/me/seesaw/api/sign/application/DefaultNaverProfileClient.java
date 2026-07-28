package kr.me.seesaw.api.sign.application;

import kr.me.seesaw.api.sign.NaverProfileClient;
import kr.me.seesaw.api.sign.dto.NaverUserProfile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DefaultNaverProfileClient implements NaverProfileClient {

    private static final String USER_INFO_URI = "https://openapi.naver.com/v1/nid/me";

    private final RestClient restClient;

    public DefaultNaverProfileClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public NaverUserProfile getProfile(String accessToken) {
        try {
            NaverProfileResponse profile = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(NaverProfileResponse.class);
            if (profile == null || profile.response() == null) {
                throw new BadCredentialsException("네이버 사용자 정보를 조회하지 못했습니다.");
            }
            return new NaverUserProfile(
                    profile.response().id(),
                    profile.response().email(),
                    profile.response().name()
            );
        } catch (RestClientException ex) {
            throw new BadCredentialsException("네이버 액세스 토큰이 유효하지 않습니다.", ex);
        }
    }

    private record NaverProfileResponse(NaverProfile response) {

    }

    private record NaverProfile(String id, String email, String name) {

    }

}
