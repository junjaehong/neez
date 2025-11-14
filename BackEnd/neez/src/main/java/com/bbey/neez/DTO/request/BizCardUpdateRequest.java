package com.bbey.neez.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "명함 기본정보 수정 요청 DTO")
public class BizCardUpdateRequest {

    @Schema(description = "명함 소유자 변경 시 사용자 IDX", example = "1")
    private Long user_idx;

    @Schema(
            description = "직접 특정 회사와 연결하고 싶을 때 companies.idx",
            example = "10",
            nullable = true
    )
    private Long company_idx;

    @Schema(description = "이름", example = "홍길동(수정)")
    private String name;

    @Schema(description = "부서", example = "플랫폼팀")
    private String department;

    @Schema(description = "직급", example = "대리")
    private String position;

    @Schema(description = "이메일", example = "newmail@naver.com")
    private String email;

    @Schema(description = "휴대폰번호", example = "010-0000-1111")
    private String mobile;

    @Schema(description = "대표번호/사무실번호", example = "02-1111-2222")
    private String tel;

    @Schema(description = "팩스번호", example = "02-333-4444")
    private String fax;

    @Schema(description = "주소", example = "서울시 성동구 뚝섬로 ...")
    private String address;
}
