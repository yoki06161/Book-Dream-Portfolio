package com.bookdream.sbb.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bookdream.sbb.event.Event;
import com.bookdream.sbb.event.EventService;
import com.bookdream.sbb.pay.Orders;
import com.bookdream.sbb.pay.OrdersService;
import com.bookdream.sbb.pay.Pay;
import com.bookdream.sbb.pay.PayService;
import com.bookdream.sbb.prod.Prod_Service;
import com.bookdream.sbb.prod_repo.Prod_Books;
import com.bookdream.sbb.prod_repo.Prod_d_Answer;
import com.bookdream.sbb.prod_repo.Prod_d_Review;
import com.bookdream.sbb.user.SiteUser;
import com.bookdream.sbb.user.UserCreateForm;
import com.bookdream.sbb.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
	// 이미 생성된 빈을 가져오기
	@Autowired
	private final UserService userService;
	private final Prod_Service prodService;
	private final EventService eventService;
	private final PayService payService;
	private final OrdersService ordersService;

	@GetMapping("/login")
	public String login() {
		return "admin/login";
	}

	@GetMapping("/user")
	public String user(Model model) {
		List<SiteUser> users = userService.getAllUsers();
		model.addAttribute("users",users);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 유저 관리 출력
			return "admin/user";
		}
	}
	
	@GetMapping("/user/{id}")
	public String userDetail(Model model, @PathVariable("id") Long id) {
		SiteUser user = userService.getUserById(id);
		model.addAttribute("user", user);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 유저 관리 상세 출력
			return "admin/user_detail";
		}
	}
	
	@GetMapping("/user/update/{id}")
	public String userUpdate(Model model, @PathVariable("id") Long id) {
		SiteUser user = userService.getUserById(id);
		model.addAttribute("user", user);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 유저 관리 상세 출력
			return "admin/user_update";
		}
	}
	
	@PostMapping("/user/update/{id}")
	public String userUpdateSuccess(Model model, @PathVariable("id") Long id, HttpServletRequest request) {
		SiteUser user = userService.getUserById(id);
		String email = request.getParameter("email");
		String role = request.getParameter("role");
		
		userService.modifyEmailOrRole(user,email, role);
		model.addAttribute("user", user);
		
		return "admin/user_detail";
	}
	
	@PostMapping("/user/delete/{id}")
	public String userDelete(Model model, @PathVariable("id") Long id) {
		SiteUser user = userService.getUserById(id);
		userService.deleteUser(user);
		
		return "redirect:/admin/user"; // 원하는 페이지로 리디렉션
	}

	@GetMapping("/event")
	public String event(Model model) {
		List<Event> events = eventService.getAllEvents();
		model.addAttribute("events",events);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 이벤트 관리 출력
			return "admin/event";
		}
	}
	
	@GetMapping("/event/{idx}")
	public String eventDetail(Model model, @PathVariable("idx") int idx) {
		Event event = eventService.getEventByIdx(idx);
		model.addAttribute("event", event);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 이벤트 관리 상세 출력
			return "admin/event_detail";
		}
	}
	
	@GetMapping("/event/update/{idx}")
	public String eventUpdate(Model model, @PathVariable("idx") int idx) {
		Event event = eventService.getEventByIdx(idx);
		model.addAttribute("event", event);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 이벤트 관리 상세 출력
			return "admin/event_update";
		}
	}
	
	@PostMapping("/event/update/{idx}")
	public String eventUpdateSuccess(Model model, @PathVariable("idx") int idx, HttpServletRequest request) {
		Event event = eventService.getEventByIdx(idx);
		String title = request.getParameter("title");
		String description = request.getParameter("description");
		String content = request.getParameter("content");
		
		eventService.modifyEmailOrRole(event, title, description, content);
		model.addAttribute("event", event);
		
		return "admin/event_detail";
	}
	
	@PostMapping("/event/delete/{idx}")
	public String eventDelete(Model model, @PathVariable("idx") int idx) {
		Event event = eventService.getEventByIdx(idx);
		eventService.deleteEvent(idx);
		
		return "redirect:/admin/event"; // 원하는 페이지로 리디렉션
	}

	@GetMapping("/prod")
	public String prod(Model model) {
		List<Prod_Books> books = prodService.getAllBooks();
		model.addAttribute("books",books);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 상품 관리 출력
			return "admin/prod";
		}
	}
	
	@GetMapping("/prod/{book_id}")
	public String prodDetail(Model model, @PathVariable("book_id") Integer book_id) {
		Prod_Books book = prodService.getProdBooks(book_id);
		model.addAttribute("book", book);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 상품 관리 상세 출력
			return "admin/prod_detail";
		}
	}

	@GetMapping("/prod_review")
	public String prod_review(Model model) {
		List<Prod_d_Review> reviews = prodService.getAllReviews();
		List<Prod_d_Answer> answers = prodService.getAnswer_List();
		model.addAttribute("reviews", reviews);
		model.addAttribute("answers", answers);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 상품 관리 상세 출력
			return "admin/prod_review";
		}
	}
	
	@PostMapping("/review/delete/{id}")
	public String reviewDelete(Model model, @PathVariable("id") int id) {
		Prod_d_Review review = prodService.getReview(id);
		prodService.deleteReview(id);
		
		return "redirect:/admin/prod_review"; // 원하는 페이지로 리디렉션
	}	
	
	@PostMapping("/answer/delete/{id}")
	public String answerDelete(Model model, @PathVariable("id") int id) {
		Prod_d_Answer answer = prodService.getAnswer(id);
		prodService.deleteAnswer(id);
		
		return "redirect:/admin/prod_review"; // 원하는 페이지로 리디렉션
	}	

	@GetMapping("/order")
	public String order(Model model) {
		List<Pay> pays = payService.getAllPays();
		model.addAttribute("pays", pays);
		
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 주문 관리 출력
			return "admin/order";
		}
	}
	
	@GetMapping("/order/{pay_id}")
	public String order_detail(Model model, @PathVariable("pay_id") String pay_id) {
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
		// 현재 인증된 사용자의 이메일 가져오기
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		// 로그인하지 않았다면, 로그인 페이지 출력
		if(email == "anonymousUser") {
			return login(); 
		} else {
			// 로그인했다면, 주문 관리 상세 출력
			return "admin/order_detail";
		}
	}
}
