package com.bbey.neez.controller.company;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.company.CompanyCreateRequest;
import com.bbey.neez.DTO.company.CompanySearchDto;
import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company Search API", description = "회사 검색 및 선택용 API")
public class CompanySearchController {

        private final CompanyRepository companyRepository;

        // =======================
        // 1) 회사 검색 API
        // =======================
        @Operation(summary = "회사 검색", description = "회사명 / 주소 / 도메인 / 홈페이지에 keyword가 포함된 회사를 검색합니다.\n\n" +
                        "- keyword: 검색어 (필수)\n" +
                        "- limit: 최대 개수 (선택, 기본 20)\n\n" +
                        "프론트에서는 이 API로 검색 후, 결과 리스트에서 하나를 선택해서 companyId로 사용하면 됩니다.")
        @GetMapping("/search")
        public ResponseEntity<ApiResponseDto<List<CompanySearchDto>>> searchCompanies(
                        @Parameter(description = "회사명 / 주소 / 도메인 / 홈페이지 검색 키워드", example = "삼성") @RequestParam("keyword") String keyword,

                        @Parameter(description = "최대 검색 결과 개수 (기본 20)", example = "20") @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
                String trimmed = (keyword == null) ? "" : keyword.trim();
                if (trimmed.isEmpty()) {
                        ApiResponseDto<List<CompanySearchDto>> res = new ApiResponseDto<>(false,
                                        "keyword 는 비워둘 수 없습니다.", null);
                        return ResponseEntity.badRequest().body(res);
                }

                if (limit <= 0 || limit > 100) {
                        limit = 20; // 최소 방어
                }

                List<Company> results = companyRepository.searchByKeyword(trimmed, PageRequest.of(0, limit));

                List<CompanySearchDto> dtoList = results.stream()
                                .map(CompanySearchDto::from)
                                .collect(Collectors.toList());

                ApiResponseDto<List<CompanySearchDto>> res = new ApiResponseDto<>(true, "ok", dtoList);
                return ResponseEntity.ok(res);
        }

        // =======================
        // 2) 회사 등록 API
        // =======================
        @Operation(summary = "회사 직접 등록", description = "검색으로 찾을 수 없는 회사를 직접 등록합니다.\n\n" +
                        "- name: 회사명 (필수)\n" +
                        "- address, homepage, bizNo, corpNo 등은 선택 입력입니다.\n" +
                        "source 필드는 자동으로 USER 로 설정됩니다.")
        @PostMapping
        public ResponseEntity<ApiResponseDto<CompanySearchDto>> createCompany(
                        @RequestBody CompanyCreateRequest request) {
                String name = request.getName() == null ? "" : request.getName().trim();
                if (name.isEmpty()) {
                        ApiResponseDto<CompanySearchDto> res = new ApiResponseDto<>(false, "회사명(name)은 필수입니다.", null);
                        return ResponseEntity.badRequest().body(res);
                }

                String address = request.getAddress() == null ? "" : request.getAddress().trim();

                // 1) 회사명 + 주소 기준으로 이미 존재하면 그대로 반환 (중복 방지)
                if (!address.isEmpty()) {
                        Optional<Company> existed = companyRepository.findFirstByNameAndAddress(name, address);
                        if (existed.isPresent()) {
                                CompanySearchDto dto = CompanySearchDto.from(existed.get());
                                ApiResponseDto<CompanySearchDto> res = new ApiResponseDto<>(true,
                                                "이미 동일한 회사 정보가 존재합니다.", dto);
                                return ResponseEntity.ok(res);
                        }
                }

                // 2) bizNo / corpNo 숫자만 추출
                String bizNo = normalizeNumber(request.getBizNo());
                String corpNo = normalizeNumber(request.getCorpNo());

                // 3) 사업자번호 / 법인번호 기준 중복 체크 (있으면 그걸 반환)
                if (bizNo != null && !bizNo.isEmpty()) {
                        Optional<Company> byBizNo = companyRepository.findByBizNo(bizNo);
                        if (byBizNo.isPresent()) {
                                CompanySearchDto dto = CompanySearchDto.from(byBizNo.get());
                                ApiResponseDto<CompanySearchDto> res = new ApiResponseDto<>(true,
                                                "동일한 사업자등록번호를 가진 회사가 이미 존재합니다.", dto);
                                return ResponseEntity.ok(res);
                        }
                }
                if (corpNo != null && !corpNo.isEmpty()) {
                        Optional<Company> byCorpNo = companyRepository.findByCorpNo(corpNo);
                        if (byCorpNo.isPresent()) {
                                CompanySearchDto dto = CompanySearchDto.from(byCorpNo.get());
                                ApiResponseDto<CompanySearchDto> res = new ApiResponseDto<>(true,
                                                "동일한 법인등록번호를 가진 회사가 이미 존재합니다.", dto);
                                return ResponseEntity.ok(res);
                        }
                }

                // 4) 새 회사 생성
                Company company = new Company();
                company.setName(name);
                company.setRepName(emptyToNull(request.getRepName()));
                company.setBizNo(bizNo);
                company.setCorpNo(corpNo);
                company.setAddress(address.isEmpty() ? null : address);
                company.setHomepage(emptyToNull(request.getHomepage()));
                company.setDomain(emptyToNull(request.getDomain()));
                company.setIndustry(emptyToNull(request.getIndustry()));
                company.setSource("USER");
                company.setLastRefreshedAt(LocalDateTime.now());
                // created_at, updated_at 은 DB DEFAULT / ON UPDATE 사용 중이면 굳이 안 건드려도 됨

                Company saved = companyRepository.save(company);

                CompanySearchDto dto = CompanySearchDto.from(saved);
                ApiResponseDto<CompanySearchDto> res = new ApiResponseDto<>(true, "created", dto);
                return ResponseEntity.ok(res);
        }

        // ===== 내부 유틸 =====

        private String normalizeNumber(String raw) {
                if (raw == null)
                        return null;
                String digits = raw.replaceAll("\\D", "");
                return digits.isEmpty() ? null : digits;
        }

        private String emptyToNull(String s) {
                if (s == null)
                        return null;
                String trimmed = s.trim();
                return trimmed.isEmpty() ? null : trimmed;
        }
}
