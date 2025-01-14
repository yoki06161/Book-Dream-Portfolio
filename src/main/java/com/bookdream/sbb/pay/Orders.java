package com.bookdream.sbb.pay;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table (name = "orders")
@Getter
@Setter
@Entity
public class Orders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)    // auto_increment
	private Integer order_id;
	
	@Column
	private int count;
	
	@Column(length=50)
	private String count_price;
	
	@Column
	private Integer book_id;
	
	@Column
	private String pay_id;
}