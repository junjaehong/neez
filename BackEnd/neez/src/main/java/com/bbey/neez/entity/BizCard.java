package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bizCards")   // 실제 테이블명에 맞춰서 사용
public class BizCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Column(name = "user_idx")
    private Long userIdx;
    private String name;
    @Column(name = "company_idx")
    private Long companyIdx;
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
    // 메모 파일 경로 (card-1.txt 이런 식)
    private String memo;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // 소프트 삭제 플래그
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // Lombok @Data가 있어서 원래는 필요 없지만,
    // 너가 수동 setter도 두고 있어서 유지해줌
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
