package kr.me.seesaw.api.rolemapping;

public class RoleMappingAdminRemovalNotAllowedException extends RuntimeException {

    public RoleMappingAdminRemovalNotAllowedException(String siteId) {
        super("관리자 권한은 스스로 해제할 수 없습니다. siteId: " + siteId);
    }

}
