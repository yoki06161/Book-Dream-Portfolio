package com.bookdream.sbb.freeboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FreeboardService {

    private final FreeboardRepository freeboardRepository;
    private Map<Long, Map<String, LocalDateTime>> viewTracker = new HashMap<>();

    @Autowired
    public FreeboardService(FreeboardRepository freeboardRepository) {
        this.freeboardRepository = freeboardRepository;
    }
    
    //조회수 가장 높은게시판
    public List<Freeboard> getTop3FreeboardByViews() {
        Pageable topThree = PageRequest.of(0, 4);
        return freeboardRepository.findTop3ByOrderByViewsDesc(topThree);
    }

    public Page<Freeboard> findAll(Pageable pageable) {
        return freeboardRepository.findAll(pageable);
    }

    public Page<Freeboard> searchByTitle(String title, Pageable pageable) {
        return freeboardRepository.findByTitleContaining(title, pageable);
    }

    public Freeboard findById(Long id) {
        return freeboardRepository.findById(id).orElse(null);
    }

    @Transactional
    public Freeboard save(Freeboard freeboard) {
        if (freeboard.getCreatedAt() == null) {
            freeboard.setCreatedAt(LocalDateTime.now());
        }
        freeboard.setUpdatedAt(LocalDateTime.now());

        try {
            Freeboard savedFreeboard = freeboardRepository.save(freeboard);
            System.out.println("Freeboard saved successfully: " + savedFreeboard);
            return savedFreeboard;
        } catch (Exception e) {
            System.out.println("Error while saving Freeboard: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            freeboardRepository.deleteById(id);
            System.out.println("Freeboard deleted successfully: " + id);
        } catch (Exception e) {
            System.out.println("Error while deleting Freeboard: " + e.getMessage());
            throw e;
        }
    }
    @Transactional
    public void incrementViews(Freeboard freeboard, String uniqueUserId) {
        Map<String, LocalDateTime> userViews = viewTracker.computeIfAbsent(freeboard.getId(), k -> new HashMap<>());
        LocalDateTime lastViewed = userViews.getOrDefault(uniqueUserId, LocalDateTime.MIN);

        if (lastViewed.isBefore(LocalDateTime.now().minusDays(1))) {
            userViews.put(uniqueUserId, LocalDateTime.now());
            freeboard.setViews(freeboard.getViews() + 1);
            freeboardRepository.save(freeboard);
        }
    }
}