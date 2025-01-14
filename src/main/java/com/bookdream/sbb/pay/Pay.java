package com.bookdream.sbb.pay;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table (name = "pay")
@Getter
@Setter
@Entity
public class Pay {
	public Pay() {
		this.order_date = LocalDateTime.now();
	}
	@Id
	private String pay_id;
	
	@Column(length = 20)
	private String email;
	
	@Column(length = 20, nullable = false)
	private String name;
	
	@Column(length = 11, nullable = false)
	private String phone;
	
	@Column(length = 8)
	private String pw;
	
	@Column(length = 5, nullable = false)
	private String post_code;
	
	@Column(length = 200, nullable = false)
	private String address;
	
	@Column(length = 200)
	private String request;
	
	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime order_date;
	
	@Column(length = 50, nullable = false)
	private String total_price;
} 