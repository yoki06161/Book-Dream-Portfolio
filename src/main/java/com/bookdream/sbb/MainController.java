package com.bookdream.sbb;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookdream.sbb.event.Event;
import com.bookdream.sbb.event.EventService;
import com.bookdream.sbb.freeboard.Freeboard;
import com.bookdream.sbb.freeboard.FreeboardService;
import com.bookdream.sbb.prod.Prod_Service;
import com.bookdream.sbb.prod_repo.Prod_Books;

@Controller
@RequestMapping("/")
public class MainController {
    
    @Autowired
    private Prod_Service prodService;
    @Autowired
    private EventService eventService;
    @Autowired
    private FreeboardService freeboardService;

    @GetMapping("/main")    
    public String index(Model model) {
        List<Prod_Books> recommendedBooks = prodService.getRecommendedBooks();
        List<Event> randomEvents = eventService.getRandomEvents();
        List<Freeboard> topFreeboards = freeboardService.getTop3FreeboardByViews();
        
        model.addAttribute("recommendedBooks", recommendedBooks);
        model.addAttribute("events", randomEvents);
        model.addAttribute("topFreeboards", topFreeboards);
        return "main";
    }
    

}
