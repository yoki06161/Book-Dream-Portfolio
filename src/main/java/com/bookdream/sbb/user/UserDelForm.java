package com.bookdream.sbb.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDelForm {

    @NotEmpty(message = "현재 비밀번호는 필수항목입니다.")
    private String currentPassword;
    private String currentEmail;
}