package com.bookdream.sbb.pay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;


public interface OrdersRepository extends JpaRepository<Orders, Integer>{
	@Query("SELECT o FROM Orders o WHERE o.pay_id = :pay_id")
	List<Orders> findByPayId(@Param("pay_id") String pay_id);

	@Modifying
	@Transactional
	@Query("DELETE FROM Orders o WHERE o.pay_id = :pay_id")
	void deleteAllByPayId(@Param("pay_id") String pay_id);
}
