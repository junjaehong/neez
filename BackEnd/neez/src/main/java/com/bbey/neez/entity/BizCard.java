package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bizCards")
public class BizCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;                   // ← PK는 Long 유지 (persist 전엔 null)

    @Column(name = "user_idx", nullable = false)
    private long userIdx;               // ← 항상 채워지니까 long

    private String name;

    @Column(name = "company_idx")
    private Long companyIdx;            // ← 회사가 없을 수 있으니까 Long 유지

    private String department;
    private String position;
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "line_number")
    private String lineNumber;

    @Column(name = "fax_number")
    private String faxNumber;

    private String address;
    private String memo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;  // ← Boolean → boolean 으로

    // 필요하면 커스텀 setter 남겨도 됨
    public void setUserIdx(long userIdx) {
        this.userIdx = userIdx;
    }

    public void setCompanyIdx(Long companyIdx) {
        this.companyIdx = companyIdx;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
