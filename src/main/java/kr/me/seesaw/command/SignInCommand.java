package kr.me.seesaw.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInCommand {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
