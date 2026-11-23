package kr.me.seesaw.api.rolemapping;

/**
 * 로그인한 사용자 본인의 역할 매핑(RoleMapping) 관련 기능
 */
public interface RoleMappingService {

    /**
     * 현재 사이트에 대한 로그인한 사용자 본인의 역할 매핑을 제거한다.
     * 계정(User) 자체나 다른 사이트의 역할 매핑은 건드리지 않는다.
     */
    void removeMyRoleMapping(String siteId);

}
