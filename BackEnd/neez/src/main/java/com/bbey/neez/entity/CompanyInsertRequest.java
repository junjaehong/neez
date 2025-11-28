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

    // 신청한 사용자 idx (Users PK)
    @Column(nullable = false)
    private Long requesterUserIdx;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
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

    @Column(length = 500)
    private String rejectReason;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private Long processedByAdminIdx;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
