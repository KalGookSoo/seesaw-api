package kr.me.seesaw.service;

import org.springframework.security.acls.model.Permission;

public interface ReplyPermissionService {

    boolean hasPermission(String replyId, Permission permission);

    boolean isOwner(String replyId);

}
