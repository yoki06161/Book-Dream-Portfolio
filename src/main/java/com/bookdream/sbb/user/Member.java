package com.bookdream.sbb.user;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    private String loginId;
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    // provider : google, kakao등이 들어감
    private String provider;
    
    // providerId : 소셜 로그인 한 유저의 고유 ID가 들어감
    private String providerId;
    
    private LocalDateTime create_date;
    
    @PrePersist
    public void prePersist() {
        this.create_date = LocalDateTime.now();
    }
    
    @Column(name = "last_name_change_date")
    private LocalDateTime lastNameChangeDate;

    private String email;
}
