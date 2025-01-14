package com.bookdream.sbb.prod_repo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name="prod_score")
@Getter
@Setter
@Entity
public class Prod_Score {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	// 책아이디
	private Integer book;
	
	// 유저 아이디? 이름? 이메일
	@Column(length = 200)
	private String user;
	
	// 책 평가 점수
	@Column
	private Integer score;
}

