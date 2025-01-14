package com.bookdream.sbb.event;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx; 


    @Column(nullable = false)
    private LocalDateTime startDate;
    private LocalDateTime endDate; // 이벤트 종료 날짜 및 시간
    private LocalDateTime postDate;
    private String id;
    private String title;
    private String content;
    private String image;
    private String description;

    // Lombok annotations @Getter and @Setter automatically generate getters and setters
}