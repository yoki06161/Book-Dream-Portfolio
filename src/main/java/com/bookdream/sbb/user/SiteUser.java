package com.bookdream.sbb.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Table(name = "site_user")
@Entity
@Getter
@Setter
public class SiteUser {

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
    
    private LocalDateTime create_date;
    
    @PrePersist
    public void prePersist() {
        this.create_date = LocalDateTime.now();
    }
    
    @Column(name = "last_name_change_date")
    private LocalDateTime lastNameChangeDate;
    
    private String provider;
    {this.provider="site";}

}