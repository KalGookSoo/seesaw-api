package kr.me.seesaw.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class SavePermissionRequest implements Serializable {

    private String targetId;

    private String roleId;

    private int mask;

}
