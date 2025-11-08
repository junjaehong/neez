// 수기 등록 요청 DTO
package com.bbey.neez.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BizCardManualRequest {

    @Schema(description = "소유자 IDX", example = "1")
    private Long user_idx;

    @Schema(description = "회사명", example = "네이버")
    private String company;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "부서", example = "개발1팀")
    private String department;

    @Schema(description = "직급", example = "주임")
    private String position;

    @Schema(description = "이메일", example = "gildong@naver.com")
    private String email;

    @Schema(description = "휴대폰번호", example = "010-1234-5678")
    private String mobile;

    @Schema(description = "대표번호/사무실번호", example = "02-987-6543")
    private String tel;

    @Schema(description = "팩스번호", example = "02-333-2222")
    private String fax;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "메모", example = "네이버 담당자, 다음 주 회의 예정")
    private String memo;
}
