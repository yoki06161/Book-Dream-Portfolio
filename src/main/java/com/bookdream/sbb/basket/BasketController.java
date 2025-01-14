package com.bookdream.sbb.basket;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.bookdream.sbb.pay.Orders;
import com.bookdream.sbb.prod.Prod_Service;
import com.bookdream.sbb.user.UserService;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import com.bookdream.sbb.prod_repo.*;

@Controller
@RequestMapping("/basket")
@RequiredArgsConstructor
public class BasketController {
	@Autowired
	private final Prod_Service prodService;
	private final BasketService basketService;

	@GetMapping("")
	//@RequestParam("book_id") Integer book_id, @RequestParam("count") Integer count, @RequestParam("count_price") String count_price
	public String list(Model model, HttpSession session) {
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 세션에 저장된 장바구니 내역 출력
		if(email == "anonymousUser") {
			return "basket/list"; 
		} else {
			// 로그인했다면 DB에 저장된 장바구니 내역 불러오기
			//세션스토리지에 저장된 정보와 테이블에 저장된 정보를 비교하여.. 동일한 상품 id가 있을 경우 기존테이블 정보 유지
//			List<Basket> baskets = basketService.getItemsByEmail(email);
//	        model.addAttribute("baskets", baskets);
//	        
//	        List<Prod_Books> books = baskets.stream()
//	                .map(basket -> prodService.getProdBooks(basket.getBook_id()))
//	                .collect(Collectors.toList());
//
//	        model.addAttribute("books", books);
			return "basket/list"; 
		}
	}

	@PostMapping("/add")
	public ResponseEntity<String> basketAdd(@RequestBody List<Map<String, Object>> dataArray) {
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(email);

		// 유저가 로그인했을때만(로그아웃했다가 로그인해도 됨!!)
		if (email != "anonymousUser") {			
			// 저장할 필드들 새로 DB에 저장
			List<Map<String, Object>> sessionData = new ArrayList<>();
			for (Map<String, Object> data : dataArray) {
				Map<String, Object> sessionEntry = new HashMap<>();
				sessionEntry.put("email", email);
				sessionEntry.put("book_id", data.get("book_id"));
				sessionEntry.put("count", data.get("count"));
				sessionEntry.put("count_price", data.get("count_price"));
				sessionData.add(sessionEntry);
			}
			try {
				basketService.saveBasketItems(sessionData, email);
				System.out.println(sessionData);
			} catch (Exception e) {
				// 중복 데이터가 있는 경우 예외 처리
				return ResponseEntity.badRequest().body("Duplicate entry detected");
			}
		}

		// 응답
		return ResponseEntity.ok("Session data saved successfully");
	}


    @PostMapping("/delete")
    public ResponseEntity<String> deleteBasketItems(@RequestBody Map<String, Object> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!"anonymousUser".equals(email)) {
            Integer book_id = (Integer) request.get("book_id");
            basketService.deleteBasketItems(book_id, email);
        }

        return ResponseEntity.ok("Item deleted successfully");
    }
}