package com.bookdream.sbb.prod_repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Prod_RArepository extends JpaRepository<Prod_d_Answer, Integer>{

}
