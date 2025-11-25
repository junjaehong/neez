package com.bbey.neez.entity;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "meetShorts")
public class MeetShort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idx;
    private long user_idx;
    private String title;
    private String shorts;
    private String summary_lang;
    private String audio_url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}