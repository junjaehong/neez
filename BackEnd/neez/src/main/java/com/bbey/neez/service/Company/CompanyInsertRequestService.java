package com.bbey.neez.service.company;

import com.bbey.neez.DTO.company.CompanyInsertRequestDto;
import com.bbey.neez.DTO.company.CreateCompanyInsertRequestDto;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.CompanyInsertRequest;
import com.bbey.neez.entity.CompanyInsertRequest.RequestStatus;
import com.bbey.neez.repository.CompanyInsertRequestRepository;
import com.bbey.neez.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompanyInsertRequestService {

    private final CompanyInsertRequestRepository requestRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public void createRequest(Long requesterUserIdx, CreateCompanyInsertRequestDto dto) {
        CompanyInsertRequest req = CompanyInsertRequest.builder()
                .requesterUserIdx(requesterUserIdx)
                .name(dto.getName())
                .address(dto.getAddress())
                .department(dto.getDepartment())
                .position(dto.getPosition())
                .phone(dto.getPhone())
                .fax(dto.getFax())
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        requestRepository.save(req);
    }

    @Transactional(readOnly = true)
    public Page<CompanyInsertRequestDto> getPendingRequests(Pageable pageable) {
        return requestRepository.findByStatus(RequestStatus.PENDING, pageable)
                .map(this::toDto);
    }

    @Transactional
    public void approve(Long requestId, Long adminIdx) {
        CompanyInsertRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        // 1. Company 중복 여부 한 번 더 체크 (이름 + 주소 기준)
        Company company = companyRepository
                .findFirstByNameAndAddress(req.getName(), req.getAddress())
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setName(req.getName());
                    c.setAddress(req.getAddress());

                    // rep_name / biz_no / corp_no / dart_corp_code / homepage 등은
                    // 지금 단계에서는 모르는 값이므로 null 유지.
                    // 필요해지면 나중에 관리자 화면에서 따로 수정 가능.

                    // confidence: 수동 등록이니까 100% 같은 느낌으로 주고 싶으면:
                    // c.setConfidence(new BigDecimal("100.00"));
                    // 아니면 그냥 null
                    c.setSource("MANUAL_REQUEST");

                    // created_at / updated_at 은
                    // DB DEFAULT / ON UPDATE로 관리 중이면 굳이 안 세팅해도 됨.
                    c.setCreatedAt(LocalDateTime.now());
                    c.setUpdatedAt(LocalDateTime.now());

                    return companyRepository.save(c);
                });

        // 2. 요청 상태 업데이트
        req.setStatus(RequestStatus.APPROVED);
        req.setProcessedByAdminIdx(adminIdx);
        req.setProcessedAt(LocalDateTime.now());
        requestRepository.save(req);
    }

    @Transactional
    public void reject(Long requestId, Long adminIdx, String reason) {
        CompanyInsertRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));

        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        req.setStatus(RequestStatus.REJECTED);
        req.setRejectReason(reason);
        req.setProcessedByAdminIdx(adminIdx);
        req.setProcessedAt(LocalDateTime.now());
        requestRepository.save(req);
    }

    private CompanyInsertRequestDto toDto(CompanyInsertRequest entity) {
        CompanyInsertRequestDto dto = new CompanyInsertRequestDto();
        dto.setId(entity.getId());
        dto.setRequesterUserIdx(entity.getRequesterUserIdx());
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setDepartment(entity.getDepartment());
        dto.setPosition(entity.getPosition());
        dto.setPhone(entity.getPhone());
        dto.setFax(entity.getFax());
        dto.setStatus(entity.getStatus().name());
        dto.setRejectReason(entity.getRejectReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setProcessedAt(entity.getProcessedAt());
        dto.setProcessedByAdminIdx(entity.getProcessedByAdminIdx());
        return dto;
    }
}
