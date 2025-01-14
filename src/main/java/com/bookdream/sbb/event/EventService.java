package com.bookdream.sbb.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookdream.sbb.DataNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // 모든 이벤트 목록 조회
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 특정 ID를 가진 이벤트 조회
    public Event getEventByIdx(int idx) {
    	Optional<Event> event = this.eventRepository.findById(idx);
    	if (event.isPresent()) {
    		return event.get();
    	}else {
    		throw new DataNotFoundException("이벤트를 찾지 못했습니다");
    	}
    }

    // 이벤트 생성
    @Transactional
    public void createEvent(Event event) {
    	 if (event.getPostDate() == null) {
             event.setPostDate(LocalDateTime.now()); // 이벤트 객체를 데이터베이스에 저장
         }        try {
             eventRepository.save(event);
             System.out.println("event saved successfully: " + event);
         } catch (Exception e) {
             System.out.println("Error while saving event: " + e.getMessage());
             throw e;
         }
    }

    // 이벤트 업데이트
    @Transactional
    public Event updateEvent(int idx, Event updatedEvent) {
        Event event = eventRepository.findById(idx)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + idx));

        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setStartDate(updatedEvent.getStartDate());
        event.setEndDate(updatedEvent.getEndDate());
        event.setImage(updatedEvent.getImage());

        return eventRepository.save(event);
    }

    // 이벤트 삭제
    @Transactional
    public void deleteEvent(int idx) {
        eventRepository.findById(idx)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + idx));
        eventRepository.deleteById(idx);
    }
    
    //랜덤 이벤트 불러오기
    public List<Event> getRandomEvents() {
        List<Event> events = eventRepository.findAll();
        Collections.shuffle(events);
        return events.subList(0, Math.min(events.size(), 2));
    }

	public void modifyEmailOrRole(Event event, String title, String description, String content) {
		event.setTitle(title);
		event.setDescription(description);
		event.setContent(content);
		this.eventRepository.save(event);
	}
}