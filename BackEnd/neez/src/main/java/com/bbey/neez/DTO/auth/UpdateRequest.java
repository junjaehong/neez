package com.bbey.neez.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회원 정보 수정 요청")
public class UpdateRequest {

    @Schema(description = "사용자 index값 (서버 내부적으로만 사용 가능, 보통 @AuthenticationPrincipal로 idx를 받음)")
    private Long idx;

    @Schema(description = "변경할 이름", example = "전재홍")
    private String name;

    @Schema(description = "변경할 전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "명함용 회사명", example = "BBEY Labs")
    private String cardCompanyName;

    @Schema(description = "회사 주소 (매칭용)", example = "서울특별시 강남구 테헤란로 123 10층")
    private String address;

    @Schema(description = "부서명", example = "개발팀")
    private String department;

    @Schema(description = "직책/직급", example = "백엔드 개발자")
    private String position;

    @Schema(description = "팩스번호", example = "02-123-4567")
    private String fax;
}
