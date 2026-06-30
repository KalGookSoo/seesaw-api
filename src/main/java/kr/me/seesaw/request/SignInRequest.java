package kr.me.seesaw.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest implements Serializable {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
