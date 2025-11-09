package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "company_esg_proxy_scores")
public class CompanyEsgProxyScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB 컬럼: company_idx
    @Column(name = "company_idx", nullable = false)
    private Long companyIdx;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
