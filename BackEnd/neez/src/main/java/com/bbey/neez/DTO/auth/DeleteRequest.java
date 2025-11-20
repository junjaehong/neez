package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원 탈퇴 요청")
public class DeleteRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    private String userId;

    @Schema(description = "비밀번호(탈퇴 확인용)", example = "P@ssw0rd!")
    private String password;
}
