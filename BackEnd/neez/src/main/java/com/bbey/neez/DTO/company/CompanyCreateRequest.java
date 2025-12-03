package com.bbey.neez.DTO.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyCreateRequest {

    @Schema(description = "회사명", example = "네즈랩스")
    private String name;

    @Schema(description = "대표자명", example = "전재홍")
    private String repName;

    @Schema(description = "사업자등록번호(숫자만 또는 하이픈 포함)", example = "123-45-67890")
    private String bizNo;

    @Schema(description = "법인등록번호(선택)", example = "1101112345678")
    private String corpNo;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123 10층")
    private String address;

    @Schema(description = "홈페이지 URL", example = "https://www.neezlab.com")
    private String homepage;

    @Schema(description = "도메인명(선택)", example = "neezlab.com")
    private String domain;

    @Schema(description = "업종 / 산업(선택)", example = "IT 서비스")
    private String industry;
}
