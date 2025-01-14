package com.bookdream.sbb.basket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;


public interface BasketRepository extends JpaRepository<Basket, Long> {
	@Query("SELECT b FROM Basket b WHERE b.book_id = :book_id AND b.email = :email")
	Optional<Basket> findByBookIdAndEmail(@Param("book_id") Integer book_id, @Param("email") String email);

	@Modifying
	@Transactional
	@Query("DELETE FROM Basket b WHERE b.book_id = :book_id AND b.email = :email")
	void deleteByBookIdAndEmail(@Param("book_id") Integer book_id, @Param("email") String email);
}