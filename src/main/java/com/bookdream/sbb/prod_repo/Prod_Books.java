package com.bookdream.sbb.prod_repo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Set;

import com.bookdream.sbb.user.SiteUser;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//두목님꺼 합치고 테이블 새로 만들어지는 오류 해결.
//테이블 명 지정함으로써 새로 안만들어진다.
@Table(name="prod_books")
@ToString
@Builder
@Getter
@Setter
@Entity

//@Builder 어노테이션과 함께 사용할 경우, @NoArgsConstructor 및 @AllArgsConstructor 어노테이션을 추가하는 것이 좋습니다.
// -> 기본생성자 생성
@NoArgsConstructor
@AllArgsConstructor

public class Prod_Books {
	//인덱스키
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer book_id;

	
	// 제목
	@Column(length=100)
	private String book_title;
	
	// 사진
	@Column(length=100)
	private String book_img;
	
	// 가격
	@Column(length=50)
	private String book_price;
	
	// 책 정보(저자)
	@Column(length=50)
	private String book_writer;
	
	// 책소개
	@Column(length=6000)
	private String book_intro;
	
	// 책 장르
	@Column(length = 100)
	private String book_genre;
	
//	@ManyToMany
//    Set<SiteUser> voter;
}