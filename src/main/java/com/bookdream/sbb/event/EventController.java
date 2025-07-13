package com.bookdream.sbb.event;

import java.io.File;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @Value("${file.upload-dir}") // 이 어노테이션 추가
    private String uploadDir;

    // 이벤트 메인 페이지를 반환하는 메서드
    @GetMapping("")
    public String event(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "Guest";
        model.addAttribute("username", username);
        model.addAttribute("events", eventService.getAllEvents());
        return "event/event"; // 이벤트 목록을 보여줄 페이지
    }

    @GetMapping("/detail/{idx}")
    public String viewEventDetail(@PathVariable("idx") int idx, Model model) {
        Event event = eventService.getEventByIdx(idx);
        model.addAttribute("event", event);
        System.out.println("Event Start Date: " + event.getStartDate());
        System.out.println("Event End Date: " + event.getEndDate());
        System.out.println("Event Post Date: " + event.getPostDate());

        return "event/detail";
    }

    @GetMapping("/create")
    public String eventForm(Model model) {
        model.addAttribute("event", new Event()); // 새 이벤트 객체를 모델에 추가
        return "event/create"; // 이벤트 작성 폼 페이지
    }

    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute Event event, BindingResult result,
                              @RequestParam("image") MultipartFile file, Model model,
                              RedirectAttributes redirectAttributes) {


        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + ".png";
            String filePath = Paths.get(uploadDir, fileName).toString();
            File destFile = new File(filePath);
            try {
                file.transferTo(destFile);
                event.setImage(fileName); // 파일 이름을 이벤트에 설정
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMsg", "이미지 업로드 실패: " + e.getMessage());
                return "event/create";
            }
        }

        eventService.createEvent(event);
        redirectAttributes.addFlashAttribute("message", "Event successfully created.");
        return "redirect:/event";
    }
}