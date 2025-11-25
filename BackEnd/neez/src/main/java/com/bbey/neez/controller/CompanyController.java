package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyDto;
import com.bbey.neez.repository.CompanyRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(
        name = "Company API",
        description =
                "회사 정보 조회 API\n\n" +
                "📌 companyId란?\n" +
                "- companies 테이블에 저장된 회사 레코드의 PK입니다.\n" +
                "- 명함(BizCard)에 매칭된 회사의 공식 정보를 나타냅니다.\n" +
                "- Swagger에서 테스트할 때는 실제 DB에 존재하는 companyId를 입력해야 합니다. (예: 1)\n\n" +
                "이 API는 다음 상황에서 사용됩니다.\n" +
                "- 명함 상세 화면에서 연결된 회사의 상세 정보를 보고 싶을 때\n" +
                "- 회사명, 사업자번호 등을 기반으로 조회된 회사 중 하나를 선택해 상세 정보를 확인할 때\n"
)
@SecurityRequirement(name = "BearerAuth")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Operation(
            summary = "회사 단건 조회",
            description = "companies 테이블의 idx를 기준으로 회사 상세 정보를 조회한다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CompanyDto>> getCompany(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(c -> {
                    CompanyDto dto = CompanyDto.from(c);
                    return ResponseEntity.ok(
                            new ApiResponseDto<>(true, "ok", dto)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponseDto<>(false, "Company not found: " + id, null))
                );
    }
}
