package kr.me.seesaw.api.attachment;

import org.springframework.security.acls.model.Permission;

public interface AttachmentPermissionService {

    boolean hasPermission(String attachmentId, Permission permission);

    boolean isOwner(String attachmentId);

}
