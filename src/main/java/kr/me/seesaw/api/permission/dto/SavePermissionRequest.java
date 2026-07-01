package kr.me.seesaw.api.permission.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SavePermissionRequest implements Serializable {

    private String targetId;

    private String roleId;

    private int mask;

}
