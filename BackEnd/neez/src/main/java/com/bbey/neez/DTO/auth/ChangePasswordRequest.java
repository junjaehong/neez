package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 변경 요청")
public class ChangePasswordRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    private String userId;

    @Schema(description = "현재 비밀번호", example = "OldP@ssw0rd!")
    private String currentPassword;

    @Schema(description = "새 비밀번호", example = "NewP@ssw0rd!")
    private String newPassword;
}
