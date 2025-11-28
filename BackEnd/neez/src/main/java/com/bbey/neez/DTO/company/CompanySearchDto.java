package com.bbey.neez.DTO.company;

import com.bbey.neez.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySearchDto {

    private Long id;
    private String name;
    private String address;
    private String homepage;
    private String source;   // DART / USER 등 구분용

    public static CompanySearchDto from(Company c) {
        return CompanySearchDto.builder()
                .id(c.getIdx())
                .name(c.getName())
                .address(c.getAddress())
                .homepage(c.getHomepage())
                .source(c.getSource())
                .build();
    }
}
