package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Auth 처리 결과")
public class AuthResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "임시 비밀번호 등 부가 정보", example = "pw1234")
    private String extra;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String extra) {
        this.success = success;
        this.message = message;
        this.extra = extra;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getExtra() {
        return extra;
    }
}
