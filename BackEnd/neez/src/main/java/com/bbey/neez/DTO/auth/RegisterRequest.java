package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원가입 요청")
public class RegisterRequest {

    @Schema(example = "jaehong")
    private String userId;

    @Schema(example = "1q2w3e4r!")
    private String password;

    @Schema(example = "전재홍")
    private String name;

    @Schema(example = "user@example.com")
    private String email;

    @Schema(example = "01012345678")
    private String phone;

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
    public String getPhone(){
        return phone;
    }
}
