package com.bbey.neez.DTO.company;

import lombok.Data;

@Data
public class CreateCompanyInsertRequestDto {
    // 유저가 신청 시 보내는 최소 필드
    private String name;
    private String address;
    private String department;
    private String position;
    private String phone;
    private String fax;
}
