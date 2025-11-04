package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Entity
public class BizCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    private String user_id;
    private String name;
    private long company_idx;
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
}