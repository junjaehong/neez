package com.bbey.neez.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    private String name;
    private String domain;
    private String industry;
    private String detail;
    private String source;
    private Long confidence;
    private String last_refrestd_at;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
