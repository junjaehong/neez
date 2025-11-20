package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "로그아웃 요청")
public class LogoutRequest {

    @Schema(example = "jaehong")
    private String userId;

    public String getUserId() {
        return userId;
    }
}
