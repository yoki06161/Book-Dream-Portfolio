package com.bookdream.sbb.prod_repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface Prod_ScoreRepository extends JpaRepository<Prod_Score, Integer> {

    // book_id 값에 해당하는 데이터들의 평균 구하기
	// @Query는 value에 있는 sql문 내보내겠단 뜻. :은 파라미터라는데 변수명이라 생각하면 된다.
	// AvgScore은 형태명. integer같은.
    @Query(value = "SELECT AVG(score) FROM prod_score WHERE book = :b_id", nativeQuery = true)
    Double findAvgScoreBybook(@Param("b_id") Integer id);
    
    // 평균 리스트 전부 내보내기
    @Query(value = "SELECT book, AVG(score) FROM prod_score group by book", nativeQuery = true)
    List<Object[]> findAvgScore();
    
    // 책 아이디에 따른 별점 리스트 출력. 총 투표자들 수 보기용
    List<Prod_Score> findBybook(Integer book_id);

    // 별점 줬었는지 확인
	Prod_Score findByBookAndUser(Integer bookId, String user);
}