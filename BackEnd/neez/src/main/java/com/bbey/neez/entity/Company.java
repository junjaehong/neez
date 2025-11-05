package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String name;
    private String domain;
    private String industry;
    private String source;
    private BigDecimal confidence;
    private LocalDateTime last_refreshed_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // 명시적 getter/setter 추가: Lombok이 동작하지 않는 환경에서도 컴파일이 되도록 방어적으로 구현
    public Long getIdx() { return this.idx; }

    public void setName(String name) { this.name = name; }

    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }
}