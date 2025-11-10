package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "비밀번호 재설정(찾기) 요청")
public class ResetPasswordRequest {

    @Schema(example = "jaehong")
    private String userId;

    @Schema(example = "user@example.com")
    private String email;

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
