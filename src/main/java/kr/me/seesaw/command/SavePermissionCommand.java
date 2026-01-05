package kr.me.seesaw.command;

import lombok.Data;

import java.io.Serializable;

@Data
public class SavePermissionCommand implements Serializable {

    private String targetId;

    private String roleId;

    private int mask;

}
