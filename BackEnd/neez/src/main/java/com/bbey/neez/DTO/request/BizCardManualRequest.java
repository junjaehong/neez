package com.bbey.neez.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizCardManualRequest {

    @Schema(description = "명함을 소유하는 사용자 IDX", example = "1", required = true)
    private Long user_idx;

    @Schema(
            description = "명함에 적힌 회사명(원문). 이 값을 기반으로 회사 자동 매칭/등록이 수행됨",
            example = "삼성생명 대구금융SFP지점",
            required = true
    )
    private String company;

    @Schema(description = "이름", example = "홍길동", required = true)
    private String name;

    @Schema(description = "부서", example = "플랫폼개발팀")
    private String department;

    @Schema(description = "직급", example = "대리")
    private String position;

    @Schema(description = "이메일", example = "hong.gd@samsung.com")
    private String email;

    @Schema(description = "휴대폰번호", example = "010-1111-2222")
    private String mobile;

    @Schema(description = "대표번호/사무실번호", example = "02-3456-7890")
    private String tel;

    @Schema(description = "팩스번호", example = "02-3456-7891")
    private String fax;

    @Schema(description = "주소", example = "대구광역시 중구 달구벌대로 2095 ...")
    private String address;

    @Schema(description = "메모 내용(텍스트)", example = "대구 법인 영업 담당자, 분기별 미팅")
    private String memo;
}
