package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원 정보 수정 요청")
public class UpdateRequest {

    @Schema(description = "사용자 ID", example = "jaehong")
    private String userId;

    @Schema(description = "변경할 이름", example = "전재홍")
    private String name;

    @Schema(description = "변경할 전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "변경할 이메일", example = "user@example.com")
    private String email;
}
