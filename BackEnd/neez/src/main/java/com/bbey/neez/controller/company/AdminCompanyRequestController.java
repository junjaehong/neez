package com.bbey.neez.controller.company;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.company.CompanyInsertRequestDto;
import com.bbey.neez.DTO.company.RejectRequestDto;
import com.bbey.neez.security.UserPrincipal;
import com.bbey.neez.service.company.CompanyInsertRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/company-requests")
@RequiredArgsConstructor
public class AdminCompanyRequestController {

    private final CompanyInsertRequestService requestService;

    // 대기 중인 회사 삽입 요청 목록
    @GetMapping
    public ApiResponseDto<Page<CompanyInsertRequestDto>> listPending(Pageable pageable) {
        Page<CompanyInsertRequestDto> page = requestService.getPendingRequests(pageable);
        return new ApiResponseDto<>(true, "OK", page);
    }

    // 승인
    @PostMapping("/{id}/approve")
    public ApiResponseDto<Object> approve(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin) {
        requestService.approve(id, admin.getIdx());
        return new ApiResponseDto<>(true, "승인되었습니다.", null);
    }

    // 반려
    @PostMapping("/{id}/reject")
    public ApiResponseDto<Object> reject(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal admin,
            @RequestBody RejectRequestDto req) {
        requestService.reject(id, admin.getIdx(), req.getReason());
        return new ApiResponseDto<>(true, "반려되었습니다.", null);
    }
}
