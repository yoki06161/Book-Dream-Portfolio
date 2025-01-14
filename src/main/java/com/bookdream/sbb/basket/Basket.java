package com.bookdream.sbb.basket;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table (name = "basket")
public class Basket {
	public Basket() {
		this.created_at = LocalDateTime.now();
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)    // auto_increment
	private Long idx;

	@Column(length = 20)
	private String email;

	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
	private LocalDateTime created_at;

	@Column(length=2)
	private Integer count;

	@Column(length=50)

	private String count_price;




	@Column

	private Integer book_id;

}