package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "비밀번호 재설정 최종 요청")
public class PasswordResetConfirmRequest {

    @Schema(description = "가입 이메일", example = "user01@example.com")
    private String email;

    @Schema(description = "이메일로 전송된 인증코드", example = "483920")
    private String code;

    @Schema(description = "새 비밀번호", example = "NewP@ssw0rd!")
    private String newPassword;
}
