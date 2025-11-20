package com.bbey.neez.DTO.auth;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청")
public class RegisterRequest {

    @Schema(description = "로그인 ID", example = "jaehong01")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String userId;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String password;

    @Schema(description = "이름", example = "전재홍")
    private String name;

    @Schema(description = "이메일 주소", example = "test@example.com")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
}
