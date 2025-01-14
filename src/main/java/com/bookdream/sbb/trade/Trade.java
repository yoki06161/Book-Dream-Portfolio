package com.bookdream.sbb.trade;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Entity
@Getter
@Setter
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    @NotNull
    private String title;

    @NotNull
    private int price;

    @NotNull
    private String info;

    private String intro;
    private LocalDateTime postdate;
    private String id;
    private String image;
    
    @NotNull
    private int originalPrice;
    
    @NotNull
    private String status = "";

    @NotNull
    private String grade;
}
