package com.bbey.neez.DTO;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.bbey.neez.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "회사 정보 DTO")
public class CompanyDto {

    @Schema(description = "회사 PK (companies.idx)", example = "3")
    private Long id;

    @Schema(description = "회사명", example = "삼성생명보험 주식회사")
    @NotBlank(message = "회사명은 필수입니다.")
    @Size(max = 200, message = "회사명은 200자를 넘을 수 없습니다.")
    private String name;

    @Schema(description = "대표자명", example = "홍길동")
    @Size(max = 50, message = "대표자명은 50자를 넘을 수 없습니다.")
    private String repName;

    @Schema(description = "사업자등록번호(숫자만)", example = "1234567890")
    @Size(max = 20, message = "사업자등록번호는 20자를 넘을 수 없습니다.")
    private String bizNo;

    @Schema(description = "법인등록번호(숫자만)", example = "1234567890123")
    @Size(max = 20, message = "법인등록번호는 20자를 넘을 수 없습니다.")
    private String corpNo;

    @Schema(description = "주소", example = "서울특별시 중구 ...")
    @Size(max = 255, message = "주소는 255자를 넘을 수 없습니다.")
    private String address;

    @Schema(description = "홈페이지", example = "https://www.samsunglife.com")
    @Size(max = 255, message = "홈페이지 URL은 255자를 넘을 수 없습니다.")
    private String homepage;

    @Schema(description = "데이터 출처", example = "bizno+fss")
    private String source;

    @Schema(description = "매칭 신뢰도 (0~1)", example = "0.85")
    private BigDecimal confidence;

    public static CompanyDto from(Company c) {
        CompanyDto dto = new CompanyDto();
        dto.id = c.getIdx();
        dto.name = c.getName();
        dto.repName = c.getRepName();
        dto.bizNo = c.getBizNo();
        dto.corpNo = c.getCorpNo();
        dto.address = c.getAddress();
        dto.homepage = c.getHomepage();
        return dto;
    }
}
