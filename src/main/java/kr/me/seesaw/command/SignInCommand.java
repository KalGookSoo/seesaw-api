package kr.me.seesaw.command;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInCommand implements Serializable {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
