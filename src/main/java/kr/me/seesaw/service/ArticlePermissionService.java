package kr.me.seesaw.service;

import org.springframework.security.acls.model.Permission;

public interface ArticlePermissionService {

    boolean hasPermission(String articleId, Permission permission);

}
