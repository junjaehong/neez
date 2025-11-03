package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.Entity;

@Data
@entity
public class users{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String id;
    private String password;
    private String name;
    private String email;
    private String phone_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}