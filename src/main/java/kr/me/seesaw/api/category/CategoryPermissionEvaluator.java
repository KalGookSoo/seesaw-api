package kr.me.seesaw.api.category;

import org.springframework.security.acls.model.Permission;

public interface CategoryPermissionEvaluator {

    boolean hasPermission(String categoryId, Permission permission);

}
