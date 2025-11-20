package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(example = "jaehong")
    private String userId;

    @Schema(example = "1q2w3e4r!")
    private String password;

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
