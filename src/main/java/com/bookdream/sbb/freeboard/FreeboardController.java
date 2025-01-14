package com.bookdream.sbb.freeboard;

import java.io.File;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/freeboard")
public class FreeboardController {

    @Autowired
    private FreeboardService freeboardService;
    @Autowired
    private CommentService commentService;


    private final String uploadDir = "C:/Users/TJ/git/Book-Dream/src/main/resources/static/image/freeboard/";

    @GetMapping("")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Freeboard> freeboards;
        if (keyword != null && !keyword.isEmpty()) {
            freeboards = freeboardService.searchByTitle(keyword, pageable);
        } else {
            freeboards = freeboardService.findAll(pageable);
        }
        model.addAttribute("freeboards", freeboards);
        return "freeboard/list"; // 템플릿 경로
    }

    @GetMapping("/form")
    public String formFreeboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        String username = principal.getName();
        model.addAttribute("username", username);
        model.addAttribute("freeboard", new Freeboard());
        return "freeboard/form"; // 템플릿 경로
    }

    @PostMapping("/form")
    public String formFreeboard(@Valid @ModelAttribute Freeboard freeboard, BindingResult result,
                                @RequestParam("image") MultipartFile file, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/user/login";
        }
        String username = principal.getName();
        freeboard.setAuthor(username); // 작성자 설정



        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + ".png";
            String filePath = Paths.get(uploadDir, fileName).toString();
            File destFile = new File(filePath);
            try {
                file.transferTo(destFile);
                freeboard.setImage(fileName); // 파일 이름을 자유게시판에 설정
            } catch (Exception e) {
                model.addAttribute("errorMsg", "이미지 업로드 실패: " + e.getMessage());
                return "freeboard/form"; // 템플릿 경로
            }
        }

        freeboardService.save(freeboard);
        return "redirect:/freeboard"; // 폼 제출 후 목록 페이지로 리다이렉트
    }

    @GetMapping("/detail/{id}")
    public String detailFreeboard(@PathVariable("id") Long id, Model model, Principal principal,
            HttpServletRequest request, HttpServletResponse response) {
        Freeboard freeboard = freeboardService.findById(id);
        if (freeboard == null) {
            return "redirect:/freeboard"; // 글이 없을 경우 목록 페이지로 리다이렉트
        }
        String uniqueUserId;
        if (principal != null) {
            uniqueUserId = principal.getName();
        } else {
            uniqueUserId = getOrSetUniqueUserId(request, response);
        }
        freeboardService.incrementViews(freeboard, uniqueUserId);
        List<Comment> comments = commentService.findByFreeboardId(id);
        model.addAttribute("freeboard", freeboard);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        
        return "freeboard/detail"; // 템플릿 경로
    }

    @PostMapping("/detail/{id}/comment")
    public String addComment(@PathVariable("id") Long id, @Valid @ModelAttribute("newComment") Comment comment, BindingResult result, Model model, Principal principal) {
        if (result.hasErrors()) {
            return "redirect:/freeboard/detail/" + id;
        }

        String username = principal.getName();
        comment.setAuthor(username);
        Freeboard freeboard = freeboardService.findById(id);
        comment.setFreeboard(freeboard);
        commentService.save(comment);

        return "redirect:/freeboard/detail/" + id;
    }
    
    @GetMapping("/edit/{id}")
    public String editFreeboardForm(@PathVariable("id") Long id, Model model, Principal principal) {
        Freeboard freeboard = freeboardService.findById(id);
        if (freeboard == null || principal == null || !principal.getName().equals(freeboard.getAuthor())) {
            return "redirect:/freeboard/list";
        }
        model.addAttribute("freeboard", freeboard);
        return "freeboard/edit";
    }

    @PostMapping("/edit/{id}")
    public String editFreeboard(@PathVariable("id") Long id, @Valid @ModelAttribute Freeboard freeboard, BindingResult result,
                                @RequestParam("image") MultipartFile file, Model model, Principal principal) {
        if (principal == null || !principal.getName().equals(freeboard.getAuthor())) {
            return "redirect:/user/login";
        }

        if (result.hasErrors()) {
            return "freeboard/edit";
        }

        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID().toString() + ".png";
            String filePath = Paths.get(uploadDir, fileName).toString();
            File destFile = new File(filePath);
            try {
                file.transferTo(destFile);
                freeboard.setImage(fileName);
            } catch (Exception e) {
                model.addAttribute("errorMsg", "이미지 업로드 실패: " + e.getMessage());
                return "freeboard/edit";
            }
        }

        freeboardService.save(freeboard);
        return "redirect:/freeboard/detail/" + id; // 수정 후 해당 게시글의 상세 페이지로 리다이렉트
    }
    private String getOrSetUniqueUserId(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("uniqueUserId".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        String uniqueUserId = UUID.randomUUID().toString();
        Cookie newCookie = new Cookie("uniqueUserId", uniqueUserId);
        newCookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(newCookie);
        return uniqueUserId;
    }
}
