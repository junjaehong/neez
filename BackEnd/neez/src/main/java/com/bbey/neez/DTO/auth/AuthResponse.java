package com.bbey.neez.dto.auth;

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

    @Schema(description = "추가 데이터(회원 정보 등)")
    private Object data;  // Users 객체나 Map 등 다양한 데이터 저장 가능

    public AuthResponse() {}

    // 기본 메시지
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 임시 비밀번호 등 문자열 데이터 반환용
    public AuthResponse(boolean success, String message, String extra) {
        this.success = success;
        this.message = message;
        this.extra = extra;
    }

    // Users 객체 등 다양한 데이터 반환용
    public AuthResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
