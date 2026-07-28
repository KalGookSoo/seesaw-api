package kr.me.seesaw.api.sign;

import kr.me.seesaw.api.sign.dto.NaverUserProfile;

public interface NaverProfileClient {

    /**
     * 네이버 액세스 토큰으로 프로필을 조회합니다. 토큰이 유효하지 않으면 네이버 서버가 요청을 거부하므로,
     * 이 호출이 성공한다는 사실 자체가 토큰의 진위를 검증합니다.
     *
     * @param accessToken 네이버 액세스 토큰
     * @return 네이버 사용자 프로필
     */
    NaverUserProfile getProfile(String accessToken);

}
