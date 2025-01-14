package com.bookdream.sbb.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank(message = "ID를 입력하세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;
    private String passwordCheck;

    @NotBlank(message = "이름을 입력하세요.")
    private String name;

    // 비밀번호 암호화 X
    public Member toEntity(){
        return Member.builder()
                .loginId(this.loginId)
                .password(this.password)
                .name(this.name)
                .role(MemberRole.USER)
                .build();
    }

    // 비밀번호 암호화
//    public Member toEntity(String encodedPassword) {
//        return Member.builder()
//                .loginId(this.loginId)
//                .password(encodedPassword)
//                .name(this.name)
//                .role(MemberRole.USER)
//                .build();
//    }
}