package com.bbey.neez.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BizCardDto {
    private Long idx;
    private Long userIdx;
    private String name;
    private String companyName;   // 회사 이름만
    private String department;
    private String position;
    private String email;
    private String phoneNumber;
    private String lineNumber;
    private String faxNumber;
    private String address;
    private String memoContent;   // 파일에서 읽은 실제 메모
}
