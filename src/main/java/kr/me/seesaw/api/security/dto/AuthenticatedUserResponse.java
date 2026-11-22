package kr.me.seesaw.api.security.dto;

import lombok.Builder;

import java.util.Collection;

@Builder
public record AuthenticatedUserResponse(String userId, String username, String email, Collection<String> authorities) {

}
