package com.bookdream.sbb.user;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModifyNameForm {

    @NotEmpty(message = "이름은 필수항목입니다.")
    @Size(min = 1, max = 10, message = "이름은 1글자 이상 10글자 이내여야 합니다.")
    @Pattern(regexp = "^[^\\s]+$", message = "이름은 공백을 포함할 수 없습니다.")
    private String newName;
    
    // 이름 변경 제한 날짜
    private LocalDateTime nameChangeLimit;
}