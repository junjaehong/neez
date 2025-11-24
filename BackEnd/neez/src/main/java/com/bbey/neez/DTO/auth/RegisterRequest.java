package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @Schema(description = "회원 아이디", example = "jaehong")
    @NotBlank
    private String userId;

    @Schema(description = "비밀번호", example = "qwer1234!")
    @NotBlank
    private String password;

    @Schema(description = "이름", example = "전재홍")
    @NotBlank
    private String name;

    @Schema(description = "이메일", example = "test@example.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;


    // ================================
    // 회사 관련 필드
    // ================================

    @Schema(description = "명함용 회사명", example = "BBEY Labs")
    private String cardCompanyName;

    @Schema(description = "소속 회사 companies.idx", example = "12")
    private Long companyIdx;

    @Schema(description = "부서명", example = "개발팀")
    private String department;

    @Schema(description = "직급명", example = "백엔드 개발자")
    private String position;

    @Schema(description = "팩스번호", example = "02-123-4567")
    private String fax;
}
