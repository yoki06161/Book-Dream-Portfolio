package com.bookdream.sbb.trade;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeRepository extends JpaRepository<Trade, Integer> {
	@Query("SELECT t FROM Trade t WHERE t.title LIKE %:kw% OR t.info LIKE %:kw%")
    Page<Trade> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
	
	// Postdate를 기준으로 역순 정렬된 모든 트레이드를 페이지로 반환
    Page<Trade> findAllByOrderByPostdateDesc(Pageable pageable);
    
    Trade findByTitle(String title);
}