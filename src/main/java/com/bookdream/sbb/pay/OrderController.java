package com.bookdream.sbb.pay;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookdream.sbb.prod.Prod_Service;
import com.bookdream.sbb.prod_repo.Prod_Books;
import com.bookdream.sbb.user.Member;
import com.bookdream.sbb.user.MemberService;
import com.bookdream.sbb.user.SiteUser;
import com.bookdream.sbb.user.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
	@Autowired
	private final PayService payService;
	private final Prod_Service prodService;
	private final OrdersService ordersService;
	private final UserService userService;
	private final MemberService memberService;

	@GetMapping("")
	public String order(Model model, Principal principal) {
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인했다면 model 추가 후 페이지로 이동
		if(email != "anonymousUser") {
			SiteUser user = this.userService.getUserByEmail(principal.getName());
			Member member = this.memberService.getLoginMemberByLoginId(principal.getName());
			
	        // 사용자 정보를 모델에 추가
	        if (member != null && user == null) {
	            model.addAttribute("name", member.getName());
	        } else if (user != null && member == null) {
	            model.addAttribute("name", user.getUsername());
	        } else if (member != null && user != null) {
	            // 사용자 정보가 둘 다 있는 경우, 우선순위에 따라 하나를 선택
	            model.addAttribute("name", user.getUsername());
	        }
		} 
		return "pay/order";
	}

	@PostMapping("/addProducts")
	public ResponseEntity<String> addProducts(@RequestBody List<Map<String, Object>> selectedItems) {
		try {
			List<Map<String, Object>> sessionData = new ArrayList<>();
			for (Map<String, Object> data : selectedItems) {
				Map<String, Object> sessionEntry = new HashMap<>();
				sessionEntry.put("pay_id", data.get("pay_id"));
				sessionEntry.put("book_id", data.get("book_id"));
				sessionEntry.put("count", data.get("count"));
				sessionEntry.put("count_price", data.get("count_price"));
				sessionData.add(sessionEntry);    
			}
			System.out.println(sessionData);

			// 데이터 검증
			if (sessionData.isEmpty()) {
				return ResponseEntity.badRequest().body("No data to save");
			}

			ordersService.saveOrdersItems(sessionData);
			return ResponseEntity.ok("Products added successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
		}
	}

	@GetMapping("/success/{imp_uid}")
	public String paySuccess(Model model, @PathVariable("imp_uid") String pay_id, Principal principal) {
		Optional<Pay> optionalPay = (Optional<Pay>) payService.getPaysById(pay_id);
	
		if (optionalPay.isPresent()) {
			model.addAttribute("pay", optionalPay.get());

			List<Orders> orders = ordersService.getOrdersById(pay_id);
			model.addAttribute("orders", orders);

			List<Prod_Books> books = orders.stream()
					.map(order -> prodService.getProdBooks(order.getBook_id()))
					.collect(Collectors.toList());

			model.addAttribute("books", books);
		} else {
			// 예외 처리 또는 에러 메시지 처리
			model.addAttribute("error", "Payment not found");
		}
		return "pay/order_success";
	}
}
