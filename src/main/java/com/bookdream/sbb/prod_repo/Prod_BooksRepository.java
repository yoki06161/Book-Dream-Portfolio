package com.bookdream.sbb.prod_repo;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// db랑 연결하는 리포지토리
// @Repository는 dao의 일부임을 알려줌? findall같은 메소드 쓰려면 써야함.
@Repository
// JpaRepository는 db만들고 지우고 수정할 수있는 메소드 제공
// JpaRepository<엔티티 클래스, pk 타입>
public interface Prod_BooksRepository extends JpaRepository<Prod_Books, Integer> {
	// 키워드 토대로 검색. 모든 책 찾는거랑은 별개
	List<Prod_Books> findAll(Specification<Prod_Books> spec);
	List<Prod_Books> findAllById(Iterable<Integer> ids);
	
}