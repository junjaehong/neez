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

    @Schema(description = "부가 정보 (임시 비밀번호 등)", example = "pw1234")
    private String extra;

    @Schema(description = "추가 데이터 (회원 정보 등)")
    private Object data;

    @Schema(description = "Access Token")
    private String accessToken;

    @Schema(description = "Refresh Token")
    private String refreshToken;

    public AuthResponse() {}

    // 기본 성공/실패
    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // extra 포함 버전
    public AuthResponse(boolean success, String message, String extra) {
        this.success = success;
        this.message = message;
        this.extra = extra;
    }

    // data 포함 버전
    public AuthResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 로그인 전용 토큰 반환용
    public AuthResponse(boolean success, String message, String accessToken, String refreshToken) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
