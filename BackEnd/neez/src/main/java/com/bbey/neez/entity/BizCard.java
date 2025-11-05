package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "bizCards")
public class BizCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private Long user_idx;
    private String name;
    private Long company_idx;
    private String department;
    private String position;
    private String email;
    private String phone_number;
    private String line_number;
    private String fax_number;
    private String address;
    private String memo;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // 방어적 setter들 (Lombok 미적용 환경 대비)
    public void setUser_idx(Long user_idx) { this.user_idx = user_idx; }
    public void setName(String name) { this.name = name; }
    public void setCompany_idx(Long company_idx) { this.company_idx = company_idx; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }
    public void setLine_number(String line_number) { this.line_number = line_number; }
    public void setFax_number(String fax_number) { this.fax_number = fax_number; }
    public void setAddress(String address) { this.address = address; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }
}