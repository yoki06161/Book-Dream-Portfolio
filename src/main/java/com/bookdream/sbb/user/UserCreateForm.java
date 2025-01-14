package com.bookdream.sbb.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {

    @NotEmpty(message = "*이름은 필수항목입니다.")
    @Size(min = 1, max = 20, message = "*이름은 1글자 이상 20글자 이내여야 합니다.")
    @Pattern(regexp = "^[^\\s]+$", message = "*이름은 공백을 포함할 수 없습니다.")
    private String username;

    @NotEmpty(message = "*비밀번호는 필수항목입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()])(?=\\S+$).{8,}$",
             message = "※비밀번호: 8자 이상, 영문 대/소문자, 숫자, 특수문자 포함, 공백 불가※")
    private String password1;

    @NotEmpty(message = "*비밀번호는 필수항목입니다.")
    private String password2;

    @NotEmpty(message = "*이메일은 필수항목입니다.")
    @Email(message = "*유효한 이메일 주소를 입력해주세요.")
    private String email;
}