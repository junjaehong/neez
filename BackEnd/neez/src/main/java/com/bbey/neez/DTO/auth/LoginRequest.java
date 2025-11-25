package com.bbey.neez.DTO.auth;

import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;

    @Schema(description = "비밀번호", example = "P@ssw0rd!")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
