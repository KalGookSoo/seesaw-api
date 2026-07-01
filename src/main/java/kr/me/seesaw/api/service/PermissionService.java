package kr.me.seesaw.api.service;

import kr.me.seesaw.api.permission.dto.SavePermissionRequest;
import kr.me.seesaw.api.permission.dto.PermissionResponse;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> getPermissionsByTargetId(String targetId);

    PermissionResponse savePermission(SavePermissionRequest command);

}
