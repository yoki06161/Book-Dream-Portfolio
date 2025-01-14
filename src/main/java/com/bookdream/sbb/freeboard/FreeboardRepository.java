package com.bookdream.sbb.freeboard;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FreeboardRepository extends JpaRepository<Freeboard, Long> {
    Page<Freeboard> findByTitleContaining(String title, Pageable pageable);
    
    @Query("SELECT e FROM Freeboard e ORDER BY e.views DESC")
    List<Freeboard> findTop3ByOrderByViewsDesc(Pageable pageable);
}
