package kr.me.seesaw.context;

import org.springframework.security.acls.model.Permission;

public interface CategoryPermissionEvaluator {

    boolean hasPermission(String categoryId, Permission permission);

}
