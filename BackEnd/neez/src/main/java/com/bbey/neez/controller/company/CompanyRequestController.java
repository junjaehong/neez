package com.bbey.neez.controller.company;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.company.CreateCompanyInsertRequestDto;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.service.company.CompanyInsertRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies/requests")
@RequiredArgsConstructor
public class CompanyRequestController {

    private final CompanyInsertRequestService requestService;

    @PostMapping
    public ApiResponseDto<Object> create(@AuthenticationPrincipal UserPrincipal user,
                                         @RequestBody CreateCompanyInsertRequestDto req) {
        requestService.createRequest(user.getIdx(), req);
        return new ApiResponseDto<>(true, "회사 삽입 신청이 접수되었습니다.", null);
    }
}
