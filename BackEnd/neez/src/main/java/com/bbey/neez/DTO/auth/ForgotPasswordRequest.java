package com.bbey.neez.DTO.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "비밀번호 재설정 코드(인증코드) 발송 요청")
public class ForgotPasswordRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    private String userId;

    @Schema(description = "등록된 이메일 주소", example = "test@example.com")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
