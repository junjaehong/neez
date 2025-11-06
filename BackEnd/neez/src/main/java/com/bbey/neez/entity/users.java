package com.bbey.neez.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // PK

    private String user_id;
    private String password;
    private String name;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // 방어용 세터 (Lombok 미작동 환경 대비)
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
    public void setUpdated_at(LocalDateTime updated_at) { this.updated_at = updated_at; }
}
