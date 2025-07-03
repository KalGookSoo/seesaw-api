package kr.me.seesaw.command;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInCommand {

    @NotBlank(message = "사용자 이름은 필수입니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
