package com.bbey.neez.DTO.company;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyInsertRequestDto {

    private Long id;

    private Long requesterUserIdx;

    private String name;
    private String address;
    private String department;
    private String position;
    private String phone;
    private String fax;

    private String status;        // PENDING / APPROVED / REJECTED
    private String rejectReason;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long processedByAdminIdx;
}
