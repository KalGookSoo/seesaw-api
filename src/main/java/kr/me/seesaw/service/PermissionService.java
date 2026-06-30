package kr.me.seesaw.service;

import kr.me.seesaw.request.SavePermissionRequest;
import kr.me.seesaw.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> getPermissionsByTargetId(String targetId);

    PermissionResponse savePermission(SavePermissionRequest command);

}
