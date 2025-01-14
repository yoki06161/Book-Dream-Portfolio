package com.bookdream.sbb.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModifyPwForm {

    @NotEmpty(message = "*새 비밀번호는 필수항목입니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()])(?=\\S+$).{8,}$",
    message = "※비밀번호: 8자 이상, 영문 대/소문자, 숫자, 특수문자 포함, 공백 불가※")
    private String newPassword1;
    
    @NotEmpty(message = "*새 비밀번호 확인은 필수항목입니다.")
    private String newPassword2;
    
    @NotEmpty(message = "*현재 비밀번호는 필수항목입니다.")
    private String currentPassword;
}