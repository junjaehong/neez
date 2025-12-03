package com.bbey.neez.controller.company;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.company.CreateCompanyInsertRequestDto;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.service.company.CompanyInsertRequestService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class CompanyRequestController {

    private final CompanyInsertRequestService requestService;

    @PostMapping
    public ApiResponseDto<Object> create(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CreateCompanyInsertRequestDto req
    ) {
        if (user == null) {
            return new ApiResponseDto<>(false, "로그인이 필요합니다.", null);
        }

        requestService.createRequest(user.getIdx(), req);
        return new ApiResponseDto<>(true, "회사 삽입 신청이 접수되었습니다.", null);
    }
}
