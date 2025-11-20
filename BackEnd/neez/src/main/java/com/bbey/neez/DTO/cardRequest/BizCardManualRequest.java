package com.bbey.neez.DTO.cardRequest;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizCardManualRequest {

    @Schema(description = "명함을 소유하는 사용자 IDX", example = "1")
    @NotNull(message = "user_idx는 필수입니다.")
    private Long user_idx;

    @Schema(description = "명함에 적힌 회사명(원문). 이 값을 기반으로 회사 자동 매칭/등록이 수행됨", example = "삼성생명 대구금융SFP지점")
    @NotBlank(message = "회사명은 필수입니다.")
    private String company;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "부서", example = "플랫폼개발팀")
    private String department;

    @Schema(description = "직급", example = "대리")
    @Size(max = 100, message = "직책은 100자를 넘을 수 없습니다.")
    private String position;

    @Schema(description = "이메일", example = "hong.gd@samsung.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "휴대폰번호", example = "010-1111-2222")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String mobile;

    @Schema(description = "대표번호/사무실번호", example = "02-3456-7890")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String tel;

    @Schema(description = "팩스번호", example = "02-3456-7891")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String fax;

    @Schema(description = "주소", example = "대구광역시 중구 달구벌대로 2095 ...")
    @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
    private String address;

    @Schema(description = "메모 내용(텍스트)", example = "대구 법인 영업 담당자, 분기별 미팅")
    private String memo;
}
