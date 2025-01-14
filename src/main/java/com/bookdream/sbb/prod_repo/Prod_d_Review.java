package com.bookdream.sbb.prod_repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.bookdream.sbb.user.SiteUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name="prod_d_review")
@Getter
@Setter
@Entity
public class Prod_d_Review {
	// db에 테이블을 만든다. 엔티티 클래스 설정먼저 하는거.
	
	// @id는 중복 안되게하는것. 댓글 기본 아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	// 책아이디
	private Integer book;
	
	// 유저 이름
	@Column(length = 200)
	private String user;
	
	// 리뷰내용
	@Column(length = 2000, columnDefinition = "TEXT")
	private String review;
	
	// 여기서 선언한 timeIs란 변수가 html에서 쓰인다. sql에는 time_is라 저장됐는데. 다른거인가?
//	private LocalDateTime timeIs;
	private LocalDate time;
	
	// 답글이랑 연결. 댓글이 부모, 답변이 자식. 원이 댓글, 매니가 답변. 
	// mappedBy값은 @ManyToOne에서 설정한 private Prod_Review review값. 즉 이름 똑같아야함
	// cascade = CascadeType.REMOVE는 댓글 지울때 답글도 지워지게.
	@OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE) 
    private List<Prod_d_Answer> r_List;
}
