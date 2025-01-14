package com.bookdream.sbb.pay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRepository  extends JpaRepository<Pay, String> { 
	List<Pay> findByEmail(String email);
}