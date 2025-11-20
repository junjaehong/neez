package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter @Setter
public class PasswordResetConfirmRequest {

    @Schema(description = "등록된 이메일 주소", example = "test@example.com")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "이메일로 전송된 인증 코드", example = "123456")
    @NotBlank(message = "인증 코드는 필수 입력 값입니다.")
    private String code;

    @Schema(description = "새로운 비밀번호", example = "abcd1234")
    @NotBlank(message = "새 비밀번호는 필수 입력 값입니다.")
    private String newPassword;
}
