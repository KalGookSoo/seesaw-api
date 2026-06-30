package kr.me.seesaw.service;

import org.springframework.security.acls.model.Permission;

public interface AttachmentPermissionService {

    boolean hasPermission(String attachmentId, Permission permission);

    boolean isOwner(String attachmentId);

}
