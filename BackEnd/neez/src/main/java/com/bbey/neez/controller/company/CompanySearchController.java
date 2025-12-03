package com.bbey.neez.controller.company;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company Search API", description = "DB에 저장된 회사 정보를 검색하는 API")
@SecurityRequirement(name = "BearerAuth")
public class CompanySearchController {

        private final CompanyRepository companyRepository;

        /**
         * 회사 키워드 검색
         * 예) GET /api/companies/search?keyword=BBEY&page=0&size=10
         */
        @GetMapping("/search")
        @Operation(summary = "회사 키워드 검색", description = "회사명 / 주소 / 홈페이지 / 업종에 대해 키워드 LIKE 검색을 수행합니다.")
        public ApiResponseDto<Object> searchCompanies(
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                PageRequest pageRequest = PageRequest.of(page, size);

                // CompanyRepository.searchByKeyword 는 Page<Company> 를 반환
                Page<Company> resultPage = companyRepository.searchByKeyword(keyword, pageRequest);

                // Page 전체를 내려주고 싶으면 resultPage 그대로 넘겨도 되고,
                // content(List<Company>)만 보내고 싶으면 getContent() 사용
                return new ApiResponseDto<>(true, "회사 검색 결과", resultPage);
                // 또는 필요 시: return new ApiResponseDto<>(true, "회사 검색 결과",
                // resultPage.getContent());
        }

        /**
         * 사업자번호(biz_no)로 회사 단건 조회
         * 예) GET /api/companies/search-by-bizno?bizNo=1234567890
         */
        @GetMapping("/search-by-bizno")
        @Operation(summary = "사업자번호로 회사 조회", description = "biz_no 컬럼으로 회사 정보를 조회합니다.")
        public ApiResponseDto<Object> searchByBizNo(@RequestParam("bizNo") String bizNo) {
                Optional<Company> companyOpt = companyRepository.findByBizNo(bizNo);

                if (!companyOpt.isPresent()) {
                        return new ApiResponseDto<>(false, "해당 사업자번호로 등록된 회사가 없습니다.", null);
                }

                return new ApiResponseDto<>(true, "회사 조회 성공", companyOpt.get());
        }

        /**
         * 법인번호(corp_no)로 회사 단건 조회
         * 예) GET /api/companies/search-by-corpno?corpNo=1101110000000
         */
        @GetMapping("/search-by-corpno")
        @Operation(summary = "법인번호로 회사 조회", description = "corp_no 컬럼으로 회사 정보를 조회합니다.")
        public ApiResponseDto<Object> searchByCorpNo(@RequestParam("corpNo") String corpNo) {
                Optional<Company> companyOpt = companyRepository.findByCorpNo(corpNo);

                if (!companyOpt.isPresent()) {
                        return new ApiResponseDto<>(false, "해당 법인번호로 등록된 회사가 없습니다.", null);
                }

                return new ApiResponseDto<>(true, "회사 조회 성공", companyOpt.get());
        }
}
