package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import javax.persistence.Table;
import javax.persistence.Column;

@Data
@Entity
@Table(name = "bizCards")
public class BizCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Column(name="user_idx")
    private Long userIdx;
    private String name;
    @Column(name="company_idx")
    private Long companyIdx;
    private String department;
    private String position;
    private String email;
    @Column(name="phone_number")
    private String phoneNumber;
    @Column(name="line_number")
    private String lineNumber;
    @Column(name="fax_number")
    private String faxNumber;
    private String address;
    private String memo;
    @Column(name="created_at")
    private LocalDateTime createdAt;
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    // 방어적 setter들 (Lombok 미적용 환경 대비)
    public void setUserIdx(Long userIdx) { this.userIdx = userIdx; }
    public void setName(String name) { this.name = name; }
    public void setCompanyIdx(Long companyIdx) { this.companyIdx = companyIdx; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }
    public void setFaxNumber(String faxNumber) { this.faxNumber = faxNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}