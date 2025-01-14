package com.bookdream.sbb.user;

import java.security.Principal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bookdream.sbb.DataNotFoundException;
import com.bookdream.sbb.pay.Orders;
import com.bookdream.sbb.pay.OrdersService;
import com.bookdream.sbb.pay.Pay;
import com.bookdream.sbb.pay.PayService;
import com.bookdream.sbb.prod.Prod_Service;
import com.bookdream.sbb.prod_repo.Prod_Books;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private MemberService memberService;

	@Autowired
	private PayService payService;

	@Autowired
	private OrdersService ordersService;

	@Autowired
	private final Prod_Service prodService;

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("loginType", "user");
		model.addAttribute("pageName", "스프링 시큐리티 로그인");

		String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iter = authorities.iterator();
		GrantedAuthority auth = iter.next();
		String role = auth.getAuthority();

		SiteUser loginMember = userService.getUserByEmail(loginId);

		if (loginMember != null) {
			model.addAttribute("name", loginMember.getUsername());
		}
		return "layout";
	}

	@GetMapping("/signup")
	public String signupForm(UserCreateForm userCreateForm) {
		return "user/signupform";
	}

	@PostMapping("/signup")
	public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "user/signupform";
		}

		if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordIncorrect", "패스워드가 다릅니다.");
			return "user/signupform";
		}

		Map<String, String> map = new HashMap<>();
		map.put("username", userCreateForm.getUsername());
		map.put("password", userCreateForm.getPassword1());
		map.put("email", userCreateForm.getEmail());

		try {
			userService.create(map);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			if (e.getMessage().contains("이미 등록된 이메일입니다.")) {
				bindingResult.rejectValue("email", "duplicateEmail", "이미 등록된 이메일입니다.");
			} else {
				bindingResult.reject("signupFailed", e.getMessage());
			}
			return "user/signupform";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "user/signupform";
		}

		return "user/loginform";
	}

	@GetMapping("/login")
	public String loginForm(Model model) {
		return "user/loginform";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/userinfo")
	public String userinfo(Model model, Principal principal) {
		SiteUser user = null;
		Member member = null;

		// 소셜 로그인 사용자를 위한 예외 처리
		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
			// user가 없으면 무시하고 member를 찾기 위해 진행
		}

		// 일반 로그인 사용자를 위한 예외 처리
		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
		} catch (DataNotFoundException e) {
			// member가 없으면 무시하고 user를 찾기 위해 진행
		}

		// 두 테이블 중 하나라도 데이터가 있는지 확인
		if (user == null && member == null) {
			// 사용자 정보가 둘 다 없는 경우에 대한 처리
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// 사용자 정보를 모델에 추가
		if (member != null && user == null) {
			model.addAttribute("name", member.getName());
			model.addAttribute("email", member.getEmail());
			if (member.getProvider().equals("kakao")) {
				model.addAttribute("provider", "카카오록 로그인");
			} else if (member.getProvider().equals("google")) {
				model.addAttribute("provider", "구글 로그인");
			} else if (member.getProvider().equals("naver")) {
				model.addAttribute("provider", "네이버 로그인");
			}
		} else if (user != null && member == null) {
			model.addAttribute("name", user.getUsername());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("provider", "BookDream 웹사이트");
		} else if (member != null && user != null) {
			// 사용자 정보가 둘 다 있는 경우, 우선순위에 따라 하나를 선택
			model.addAttribute("name", user.getUsername());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("provider", "BookDream 웹사이트");
		}

		model.addAttribute("loginType", "user");

		return "user/userinfo";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modifynameform")
	public String modifynameform(UserModifyNameForm userModifyNameForm, Model model, Principal principal) {
		SiteUser user = null;
		Member member = null;

		// 소셜 로그인 사용자를 위한 예외 처리
		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
			// user가 없으면 무시하고 member를 찾기 위해 진행
		}

		// 일반 로그인 사용자를 위한 예외 처리
		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
		} catch (DataNotFoundException e) {
			// member가 없으면 무시하고 user를 찾기 위해 진행
		}

		// 두 테이블 중 하나라도 데이터가 있는지 확인
		if (user == null && member == null) {
			// 사용자 정보가 둘 다 없는 경우에 대한 처리
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// 사용자 정보를 모델에 추가
		if (member != null && user == null) {
			model.addAttribute("name", member.getName());
		} else if (user != null && member == null) {
			model.addAttribute("name", user.getUsername());
		} else if (member != null && user != null) {
			// 사용자 정보가 둘 다 있는 경우, 우선순위에 따라 하나를 선택
			model.addAttribute("name", user.getUsername());
		}

		return "user/modifynameform";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modifynameform")
	public String modifynameform(@Valid UserModifyNameForm userModifyNameForm, BindingResult bindingResult,
			Principal principal, Model model) {
		SiteUser user = null;
		Member member = null;

		// 소셜 로그인 사용자를 위한 예외 처리
		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
			// user가 없으면 무시하고 member를 찾기 위해 진행
		}

		// 일반 로그인 사용자를 위한 예외 처리
		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
		} catch (DataNotFoundException e) {
			// member가 없으면 무시하고 user를 찾기 위해 진행
		}

		// 두 테이블 중 하나라도 데이터가 있는지 확인
		if (user == null && member == null) {
			// 사용자 정보가 둘 다 없는 경우에 대한 처리
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// 현재 이름을 모델에 추가
		if (member != null && user == null) {
			model.addAttribute("name", member.getName());
		} else if (user != null && member == null) {
			model.addAttribute("name", user.getUsername());
		} else if (member != null && user != null) {
			// 사용자 정보가 둘 다 있는 경우, 우선순위에 따라 하나를 선택
			model.addAttribute("name", user.getUsername());
		}

		if (bindingResult.hasErrors()) {
			return "user/modifynameform";
		}

		if (user != null && user.getLastNameChangeDate() != null) {
			if (LocalDateTime.now().isBefore(user.getLastNameChangeDate().plusDays(14))) {
				bindingResult.rejectValue("nameChangeLimit", "beforeNameChangeLimit", "이름을 변경한 지 14일이 지나지 않았습니다.");
				return "user/modifynameform";
			}
		}

		if (member != null && member.getLastNameChangeDate() != null) {
			if (LocalDateTime.now().isBefore(member.getLastNameChangeDate().plusDays(14))) {
				bindingResult.rejectValue("nameChangeLimit", "beforeNameChangeLimit", "이름을 변경한 지 14일이 지나지 않았습니다.");
				return "user/modifynameform";
			}
		}

		if (user != null && userModifyNameForm.getNewName().equals(user.getUsername())) {
			bindingResult.rejectValue("newName", "sameAsCurrentName", "현재 이름과 같습니다.");
			return "user/modifynameform";
		}

		if (member != null && userModifyNameForm.getNewName().equals(member.getName())) {
			bindingResult.rejectValue("newName", "sameAsCurrentName", "현재 이름과 같습니다.");
			return "user/modifynameform";
		}

		try {
			// 사용자 정보를 모델에 추가
			if (member != null && user == null) {
				memberService.modifySocialName(member, userModifyNameForm.getNewName());
			} else if (user != null && member == null) {
				userService.modifySiteName(user, userModifyNameForm.getNewName());
			} else if (member != null && user != null) {
				// 사용자 정보가 둘 다 있는 경우, 우선순위에 따라 하나를 선택
				userService.modifySiteName(user, userModifyNameForm.getNewName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("modifyNameFailed", e.getMessage());
			return "user/modifynameform";
		}

		return "redirect:/user/userinfo";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modifypwform")
	public String modifypwform(UserModifyPwForm userModifyForm, Principal principal, Model model) {
		SiteUser user = null;
		Member member = null;
		boolean isSocialLogin = false;

		// 소셜 로그인 사용자를 찾기 위한 처리
		try {
			user = this.userService.getUserByEmail(principal.getName());
			// user가 존재하면 소셜 로그인으로 간주
			isSocialLogin = false;
		} catch (DataNotFoundException e) {
			// user가 없으면 일반 로그인 여부를 확인하기 위해 계속 진행
		}

		// 일반 로그인 사용자를 찾기 위한 처리
		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
			if (member != null) {
				isSocialLogin = true;
			}
		} catch (DataNotFoundException e) {
			// member가 없으면 무시
		}

		// 두 테이블 중 하나라도 데이터가 있는지 확인
		if (user == null && member == null) {
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// isSocialLogin 모델에 추가
		model.addAttribute("isSocialLogin", isSocialLogin);
		model.addAttribute("userModifyPwForm", userModifyForm);

		return "user/modifypwform";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modifypwform")
	public String modifypwform(@Valid UserModifyPwForm userModifyPwForm, BindingResult bindingResult,
			Principal principal, Model model) {
		SiteUser user = null;
		Member member = null;
		boolean isSocialLogin = false;

		// 소셜 로그인 사용자를 위한 예외 처리
		try {
			user = this.userService.getUserByEmail(principal.getName());
			isSocialLogin = false;
		} catch (DataNotFoundException e) {
			// user가 없으면 일반 로그인 여부를 확인하기 위해 계속 진행
		}

		// 일반 로그인 사용자를 위한 예외 처리
		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
			if (member != null) {
				isSocialLogin = true;
			}
		} catch (DataNotFoundException e) {
			// member가 없으면 무시
		}

		// 두 테이블 중 하나라도 데이터가 있는지 확인
		if (user == null && member == null) {
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// 소셜 로그인 사용자일 경우 비밀번호 변경을 허용하지 않음
		if (isSocialLogin) {
			bindingResult.reject("modifyPasswordFailed", "소셜 로그인 사용자는 비밀번호 변경이 불가합니다.");
			model.addAttribute("isSocialLogin", isSocialLogin);
			return "user/modifypwform";
		}

		if (bindingResult.hasErrors()) {
			model.addAttribute("isSocialLogin", isSocialLogin);
			return "user/modifypwform";
		}

		if (!this.userService.isSamePassword(user, userModifyPwForm.getCurrentPassword())) {
			bindingResult.rejectValue("currentPassword", "notCurrentPassword", "현재 비밀번호와 일치하지 않습니다.");
			model.addAttribute("isSocialLogin", isSocialLogin);
			return "user/modifypwform";
		}

		if (!userModifyPwForm.getNewPassword1().equals(userModifyPwForm.getNewPassword2())) {
			bindingResult.rejectValue("newPassword2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
			model.addAttribute("isSocialLogin", isSocialLogin);
			return "user/modifypwform";
		}

		try {
			userService.modifyPassword(user, userModifyPwForm.getNewPassword1());
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("modifyPasswordFailed", e.getMessage());
			model.addAttribute("isSocialLogin", isSocialLogin);
			return "user/modifypwform";
		}

		return "redirect:/";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/userdel")
	public String userdel(UserDelForm userDelForm, Principal principal, Model model) {
		SiteUser user = null;
		Member member = null;
		boolean isSocialLogin = false;

		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
			// user가 없으면 계속 진행
		}

		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
			if (member != null) {
				isSocialLogin = true;
			}
		} catch (DataNotFoundException e) {
			// member가 없으면 무시
		}

		if (user == null && member == null) {
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		model.addAttribute("isSocialLogin", isSocialLogin);
		model.addAttribute("userDelForm", userDelForm);
		return "user/userdel";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/userdel")
	public String userdel(@Valid UserDelForm userDelForm, BindingResult bindingResult, Principal principal, Model model,
			HttpSession session) {
		SiteUser user = null;
		Member member = null;
		boolean isSocialLogin = false;

		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
			// user가 없으면 계속 진행
		}

		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
			if (member != null) {
				isSocialLogin = true;
			}
		} catch (DataNotFoundException e) {
			// member가 없으면 무시
		}

		if (user == null && member == null) {
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		model.addAttribute("isSocialLogin", isSocialLogin);
		model.addAttribute("userDelForm", userDelForm);

		if (!isSocialLogin) {
			if (bindingResult.hasErrors()) {
				return "user/userdel";
			}
		}

		if (!isSocialLogin) {
			if (!this.userService.isSamePassword(user, userDelForm.getCurrentPassword())) {
				bindingResult.rejectValue("currentPassword", "notCurrentPassword", "현재 비밀번호와 일치하지 않습니다.");
				return "user/userdel";
			}

			try {
				userService.deleteUser(user);
			} catch (Exception e) {
				e.printStackTrace();
				bindingResult.reject("deleteUserFailed", e.getMessage());
				return "user/userdel";
			}
		} else {
			try {
				memberService.deleteUser(member);
			} catch (Exception e) {
				e.printStackTrace();
				bindingResult.reject("deleteUserFailed", e.getMessage());
				return "user/userdel";
			}
		}

		session.invalidate();
		SecurityContextHolder.clearContext();
		return "redirect:/";
	}

	@GetMapping("/userbuy")
	public String userbuy(Model model, Principal principal) {
		String email = principal.getName();
		SiteUser user = null;
		Member member = null;

		// 이메일에 따라 Pay 리스트를 가져옵니다.
		List<Pay> payList = payService.getPayByEmail(email);

		// 모든 pay_id에 대해 Orders를 조회합니다.
		List<Orders> allOrders = new ArrayList<>();
		Map<String, String> finalPrices = new HashMap<>(); // 변경된 부분: 총 가격을 포맷팅된 문자열로 저장

		for (Pay pay : payList) {
			List<Orders> ordersList = ordersService.getOrdersById(pay.getPay_id());
			allOrders.addAll(ordersList);

			// 결제 별 최종 가격 계산
			int totalPrice = ordersList.stream().mapToInt(order -> {
				// count_price가 문자열 형식인 경우 처리
				String countPriceStr = order.getCount_price(); // countPrice를 가져온다고 가정
				return convertPriceStringToInt(countPriceStr);
			}).sum();

			// 총 가격을 문자열로 포맷하여 저장
			String formattedPrice = formatPrice(totalPrice);
			finalPrices.put(pay.getPay_id(), formattedPrice);
		}

		// 모든 Orders에서 book_id를 수집합니다.
		List<Integer> bookIds = allOrders.stream().map(Orders::getBook_id).collect(Collectors.toList());

		// 수집한 book_id 리스트로 Prod_Books를 조회합니다.
		List<Prod_Books> allBooks = prodService.getAllProdBooks(bookIds);

		// 사용자 정보 조회
		try {
			user = this.userService.getUserByEmail(principal.getName());
		} catch (DataNotFoundException e) {
		}

		try {
			member = this.memberService.getLoginMemberByLoginId(principal.getName());
		} catch (DataNotFoundException e) {
		}

		if (user == null && member == null) {
			throw new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다.");
		}

		// 모델에 데이터 추가
		if (member != null) {
			model.addAttribute("name", member.getName());
		} else if (user != null) {
			model.addAttribute("name", user.getUsername());
		}

		if (!payList.isEmpty()) {
			// pay_id에 따른 orders 필터링
			Map<String, List<Orders>> ordersByPayId = new HashMap<>();
			for (Pay pay : payList) {
				List<Orders> ordersList = ordersService.getOrdersById(pay.getPay_id());
				ordersByPayId.put(pay.getPay_id(), ordersList);
			}
			model.addAttribute("payList", payList);
			model.addAttribute("ordersByPayId", ordersByPayId);
			model.addAttribute("allBooks", allBooks);
			model.addAttribute("finalPrices", finalPrices);
		} else {
			model.addAttribute("None", "구매하신 상품이 없습니다.");
			List<Prod_Books> recommendedBooks = prodService.getRecommendedBooks();
			model.addAttribute("recommendedBooks", recommendedBooks);
		}

		return "user/userbuy";
	}

	private int convertPriceStringToInt(String priceStr) {
		// "XX,XXX원" 형식의 문자열에서 숫자 부분만 추출하여 정수로 변환
		if (priceStr == null || priceStr.isEmpty()) {
			return 0;
		}
		// "원"과 "," 제거 후 숫자로 변환
		String cleanedPrice = priceStr.replace("원", "").replace(",", "").trim();
		try {
			return Integer.parseInt(cleanedPrice);
		} catch (NumberFormatException e) {
			// 숫자 변환 오류 발생 시 기본값 0 반환
			return 0;
		}
	}

	private String formatPrice(int price) {
		// 숫자를 "XX,XXX원" 형식으로 포맷팅
		NumberFormat formatter = NumberFormat.getInstance(Locale.KOREA);
		return formatter.format(price) + "원";
	}

}