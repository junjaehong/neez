package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    private String name;
    private String domain;
    private String industry;
    private String source;
    private BigDecimal confidence;
    private LocalDateTime last_refreshed_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}