// CreateCompanyInsertRequestDto: 사용자 신청용
package com.bbey.neez.DTO.company;

import lombok.Data;

@Data
public class CreateCompanyInsertRequestDto {
    private String name;
    private String address;
    private String department;
    private String position;
    private String phone;
    private String fax;
}
