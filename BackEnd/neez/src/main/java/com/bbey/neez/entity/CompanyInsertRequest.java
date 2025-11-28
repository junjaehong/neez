package com.bbey.neez.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_insert_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInsertRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청한 사용자 (users.idx)
    @Column(name = "requester_user_idx", nullable = false)
    private Long requesterUserIdx;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String position;

    @Column(length = 50)
    private String phone;

    @Column(length = 50)
    private String fax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status; // PENDING, APPROVED, REJECTED

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by_admin_idx")
    private Long processedByAdminIdx;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
