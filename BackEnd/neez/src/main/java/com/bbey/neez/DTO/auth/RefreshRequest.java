package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class RefreshRequest {

    @Schema(description = "Refresh Token")
    @NotBlank(message = "Refresh Token은 필수 입력 값입니다.")
    private String refreshToken;
}
