package com.bookdream.sbb.prod_repo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.bookdream.sbb.user.SiteUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

// 두목님꺼 합치고 테이블 새로 만들어지는 오류 해결.
// 테이블 명 지정함으로써 새로 안만들어진다.
@Table (name="prod_d_answer")
@Getter
@Setter
@Entity
public class Prod_d_Answer {

	// 답글 기본아이디
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	// 유저 이름
	@Column(length = 200)
	private String user;
	
	// 답변
	@Column(columnDefinition = "TEXT")
	private String answer;
	
	// 작성 시간
	private LocalDate time;
	
	// 댓글이랑 연결. 댓글이 부모, 답변이 자식. 매니가 답변, 원이 댓글
	// 댓글 아이디랑 연결된다.
	@ManyToOne
	private Prod_d_Review review;
}

