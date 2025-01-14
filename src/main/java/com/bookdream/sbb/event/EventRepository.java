package com.bookdream.sbb.event;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> { 

    // 특정 키워드를 포함하는 이벤트 검색
    @Query("SELECT e FROM Event e WHERE e.title LIKE %:kw% OR e.description LIKE %:kw%")
    Page<Event> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
    
    // 이벤트 시작 날짜를 기준으로 역순 정렬된 모든 이벤트를 페이지로 반환
    Page<Event> findAllByOrderByStartDateDesc(Pageable pageable);
    
    // 이벤트 제목을 기반으로 단일 이벤트 검색
    Event findByTitle(String title);
    
    //가장 최근에 작성한 이벤트
    @Query("SELECT e FROM Event e ORDER BY e.postDate DESC")
    List<Event> findTop2ByOrderByPostDateDesc();

    // 특정 날짜 범위 내의 이벤트를 찾는 메서드
    @Query("SELECT e FROM Event e WHERE e.startDate >= :start AND e.endDate <= :end")
    Page<Event> findAllByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);
}
