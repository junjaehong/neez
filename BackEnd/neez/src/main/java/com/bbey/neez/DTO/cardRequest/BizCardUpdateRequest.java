package com.bbey.neez.DTO.cardRequest;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BizCardUpdateRequest {

    @Size(max = 100, message = "이름은 100자를 넘을 수 없습니다.")
    private String name;

    @Size(max = 200, message = "회사명은 200자를 넘을 수 없습니다.")
    private String cardCompanyName;

    // companies.idx
    private Long company_idx;

    @Size(max = 100, message = "부서는 100자를 넘을 수 없습니다.")
    private String department;

    @Size(max = 100, message = "직책은 100자를 넘을 수 없습니다.")
    private String position;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 200, message = "이메일은 200자를 넘을 수 없습니다.")
    private String email;

    @Size(max = 50, message = "휴대폰 번호는 50자를 넘을 수 없습니다.")
    private String phoneNumber;

    @Size(max = 50, message = "대표 번호는 50자를 넘을 수 없습니다.")
    private String lineNumber;

    @Size(max = 50, message = "팩스 번호는 50자를 넘을 수 없습니다.")
    private String faxNumber;

    @Size(max = 500, message = "주소는 500자를 넘을 수 없습니다.")
    private String address;

    // 회사 자동 재매칭 여부
    private Boolean rematchCompany;
}
