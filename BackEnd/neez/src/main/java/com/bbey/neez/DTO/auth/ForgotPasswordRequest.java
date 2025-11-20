package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "비밀번호 재설정 코드(인증코드) 발송 요청")
public class ForgotPasswordRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    private String userId;

    @Schema(description = "가입 이메일", example = "user01@example.com")
    private String email;
}
