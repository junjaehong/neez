package com.bbey.neez.DTO.cardRequest;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BizCardManualRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자를 넘을 수 없습니다.")
    private String name;

    @NotBlank(message = "회사명은 필수입니다.")
    @Size(max = 200, message = "회사명은 200자를 넘을 수 없습니다.")
    private String company;

    @Size(max = 100, message = "부서는 100자를 넘을 수 없습니다.")
    private String department;

    @Size(max = 100, message = "직책은 100자를 넘을 수 없습니다.")
    private String position;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 200, message = "이메일은 200자를 넘을 수 없습니다.")
    private String email;

    @Size(max = 50, message = "휴대폰 번호는 50자를 넘을 수 없습니다.")
    private String mobile;

    @Size(max = 50, message = "대표 번호는 50자를 넘을 수 없습니다.")
    private String tel;

    @Size(max = 50, message = "팩스 번호는 50자를 넘을 수 없습니다.")
    private String fax;

    @Size(max = 500, message = "주소는 500자를 넘을 수 없습니다.")
    private String address;

    @Size(max = 2000, message = "메모는 2000자를 넘을 수 없습니다.")
    private String memo;
}
