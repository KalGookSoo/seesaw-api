package kr.me.seesaw.service;

import kr.me.seesaw.model.PermissionModel;

import java.util.List;

public interface PermissionService {

    List<PermissionModel> getPermissionsByTargetId(String targetId);

}
