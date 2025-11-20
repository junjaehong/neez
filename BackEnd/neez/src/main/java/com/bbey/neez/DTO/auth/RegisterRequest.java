package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class RegisterRequest {

    @Schema(description = "로그인 ID", example = "neez_user01")
    private String userId;

    @Schema(description = "비밀번호", example = "P@ssw0rd!")
    private String password;

    @Schema(description = "이름", example = "전재홍")
    private String name;

    @Schema(description = "이메일", example = "user01@example.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
}
