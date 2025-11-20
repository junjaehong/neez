package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "아이디 찾기 요청")
public class FindIdRequest {

    @Schema(example = "전재홍")
    private String name;

    @Schema(example = "user@example.com")
    private String email;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
