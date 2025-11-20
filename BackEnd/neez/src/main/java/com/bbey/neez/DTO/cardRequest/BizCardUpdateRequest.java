// 명함 기본정보 수정 DTO
package com.bbey.neez.DTO.cardRequest;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizCardUpdateRequest {

    @Schema(description = "명함 소유자 변경 시", example = "1")
    private Long user_idx;

    @Schema(description = "명함에 적힌 회사명(원문) 수정", example = "삼성생명 대구금융SFP지점")
    private String company; // cardCompanyName 용

    @Schema(description = "회사 IDX를 직접 지정할 때", example = "10")
    private Long company_idx;

    @Schema(description = "이름", example = "홍길동(수정)")
    @Size(max = 50, message = "이름은 50자를 넘을 수 없습니다.")
    private String name;

    @Schema(description = "부서", example = "플랫폼팀")
    private String department;

    @Schema(description = "직급", example = "대리")
    @Size(max = 100, message = "직책은 100자를 넘을 수 없습니다.")
    private String position;

    @Schema(description = "이메일", example = "newmail@naver.com")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "휴대폰번호", example = "010-0000-1111")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String mobile;

    @Schema(description = "대표번호/사무실번호", example = "02-1111-2222")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String tel;

    @Schema(description = "팩스번호", example = "02-333-4444")
    @Size(max = 50, message = "전화번호는 50자를 넘을 수 없습니다.")
    private String fax;

    @Schema(description = "주소", example = "서울시 성동구 뚝섬로 ...")
    @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
    private String address;

    @Schema(description = "회사 정보 다시 자동 매칭 수행 여부", example = "true")
    private Boolean rematchCompany;
}
