package com.bookdream.sbb.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class KakaoUser {

	public KakaoUser() {
	}
	
	public KakaoUser(Long id, String username, String password, String email, String role, LocalDateTime createDate) {
	    this.id = id;
	    this.username = username;
	    this.password = password;
	    this.email = email;
	    this.role =  "User";
	    this.createDate = LocalDateTime.now();;
	}


	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;
    
    @Column(unique = true)
    private String email;

    private String role;

    {
        this.role = "User";
    }
    
    private LocalDateTime createDate;
}