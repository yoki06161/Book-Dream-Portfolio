package com.bookdream.sbb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bookdream.sbb.prod_repo.Prod_d_Answer;
import com.bookdream.sbb.prod_repo.Prod_d_Review;
import com.bookdream.sbb.prod_repo.Prod_RArepository;
import com.bookdream.sbb.prod_repo.Prod_RErepository;

import jakarta.transaction.Transactional;

@SpringBootTest
class BookDreamApplicationTests {

	@Autowired
	private Prod_RErepository re_repo;
	
	@Autowired
	private Prod_RArepository ra_repo;
	
	@Transactional
	@Test
	void testJpa() {
		// 데이터 넣기
//		Prod_EReview pr = new Prod_EReview();
//		pr.setReview("재밌는 책이에요.");
//		pr.setUser("홍길동");
//		pr.setTime(LocalDateTime.now());
//        this.re_repo.save(pr);  // 첫번째 질문 저장
//
//        Prod_EReview pr2 = new Prod_EReview();
//        pr2.setReview("인생책");
//        pr2.setUser("홍길동2");
//        pr2.setTime(LocalDateTime.now());
//        this.re_repo.save(pr2);  // 두번째 질문 저장
		
		// 조회
//        List<Prod_EReview> all = this.re_repo.findAll();
//        assertEquals(2, all.size());
//
//        Prod_EReview q = all.get(0);
//        assertEquals("재밌는 책이에요.", q.getReview());
        
		// 수정
//		Optional<Prod_Review> op = this.re_repo.findById(1);
		// assertTrue는 if문같은거. ispresnt는 존재한다란 뜻. 즉 아이디 1인 데이터가 존재하는지 여부묻는거.
		// 만약 저기서 op값이 존재하지 않을시 밑의코드 다 실행안되고 오류뜬다.
//		assertTrue(op.isPresent());
		// 테이블 갖고 오는거. select * from이랑 같다 보면됨
//		Prod_Review p = op.get();
//		p.setSubject("제목 수정 테스트");
//		this.re_repo.save(p);
		
		// 아이디 1번 삭제
		// 리스트 길이가 2줄 맞는지 테스트
//		assertEquals(2, this.re_repo.count());
//		Optional<Prod_Review> op = this.re_repo.findById(1);
//		assertTrue(op.isPresent());
//		Prod_Review p = op.get();
//		this.re_repo.delete(p);
//		assertEquals(1, this.re_repo.count());
		
		// 답변데이터 저장
//		Optional<Prod_Review> op = this.re_repo.findById(2);
//		assertTrue(op.isPresent());
//		Prod_Review p = op.get();
//		
//		Prod_RAnswer a = new Prod_RAnswer();
//		a.setContent2("답변2 테스트.");
//		a.setReview(p);
//		a.setA_time(LocalDateTime.now());
//		this.ra_repo.save(a);
		
		// 조회
//		Optional<Prod_RAnswer> op = this.ra_repo.findById(1);
//		assertTrue(op.isPresent());
//		Prod_RAnswer a = op.get();
//		// assertEquals는 두값이 같은지 비교하는 메서드
//		// id가 2인지 비교하는 거다. 기대값 2와 실제값 a.getReview().getId()을 비교.
//		assertEquals(2, a.getReview().getId());
		
		// 답변 만들기?
//		Optional<Prod_Review> op = this.re_repo.findById(2);
//		assertTrue(op.isPresent());
//		Prod_Review pr = op.get();
//		
//		List<Prod_RAnswer> a_list = pr.getA_List();
//		
//		assertEquals(1, a_list.size());
//		assertEquals("id 자동 생성이랍니다.", a_list.get(0).getContent2());
	}

}
