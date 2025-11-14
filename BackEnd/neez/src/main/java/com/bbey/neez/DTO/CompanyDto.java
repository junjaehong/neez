package com.bbey.neez.DTO;

import java.math.BigDecimal;

import com.bbey.neez.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회사 정보 DTO (companies 테이블 1 row)")
public class CompanyDto {

    @Schema(description = "회사 고유 ID (companies.idx)", example = "3")
    private Long id;

    @Schema(description = "회사명", example = "삼성생명보험주식회사")
    private String name;

    @Schema(description = "대표자명", example = "홍길동")
    private String repName;

    @Schema(description = "사업자등록번호(10자리, 숫자만)", example = "2208112345")
    private String bizNo;

    @Schema(description = "법인등록번호(13자리, 숫자만)", example = "1101111234567")
    private String corpNo;

    @Schema(description = "본점 주소", example = "서울특별시 중구 을지로 00 ...")
    private String address;

    @Schema(description = "홈페이지 URL", example = "https://www.samsunglife.com")
    private String homepage;

    @Schema(
            description = "회사 정보 출처 (예: BIZNO, FSS, MANUAL)",
            example = "FSS",
            nullable = true
    )
    private String source;

    @Schema(
            description = "회사 정보 신뢰도(0.00~1.00)",
            example = "0.92",
            nullable = true
    )
    private BigDecimal confidence;

    public static CompanyDto from(Company c) {
        CompanyDto dto = new CompanyDto();
        dto.id = c.getIdx();   // PK
        dto.name = c.getName();
        dto.repName = c.getRepName();
        dto.bizNo = c.getBizNo();
        dto.corpNo = c.getCorpNo();
        dto.address = c.getAddress();
        dto.homepage = c.getHomepage();
        return dto;
    }
}
