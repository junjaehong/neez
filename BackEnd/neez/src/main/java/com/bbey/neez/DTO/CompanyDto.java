package com.bbey.neez.DTO;

import com.bbey.neez.entity.Company;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompanyDto {

    private Long id;
    private String name;
    private String repName;
    private String bizNo;
    private String corpNo;
    private String address;
    private String homepage;

    public static CompanyDto from(Company c) {
        CompanyDto dto = new CompanyDto();
        dto.id = c.getIdx();        // PK
        dto.name = c.getName();
        dto.repName = c.getRepName();
        dto.bizNo = c.getBizNo();
        dto.corpNo = c.getCorpNo();
        dto.address = c.getAddress();
        dto.homepage = c.getHomepage();
        return dto;
    }
}
