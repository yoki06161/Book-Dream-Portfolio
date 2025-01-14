package com.bookdream.sbb.prod;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookdream.sbb.prod_repo.Prod_Books;
import com.bookdream.sbb.prod_repo.Prod_d_Review;

import lombok.RequiredArgsConstructor;


// 스프링 실행시 로그인창 안뜨게한다
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
// 기본적으로 prod를 쓰게 만듬. 만약 이거 지우면 밑에 링크들 다 앞에 /prod붙여줘야한다.
@RequestMapping("/prod")
// final필드 자동 생성용
@RequiredArgsConstructor
@Controller
public class Prod_Controller {

	// @Autowired 이미 생성된 빈을 가져온단 뜻
	@Autowired
	private final Prod_Service prodService;
	
	// 제품 리스트
	// prod로 들어오는 주소 여기로
	@GetMapping("")
	// 자바에서 html로 데이터 전달할때 쓰는게 model
	// defaultValue는 kw값이 없을 경우 오류 안뜨게.
	public String prod_list(Model model, @RequestParam(value = "kw", defaultValue = "") String kw, 
			@RequestParam(value = "genre", defaultValue = "") String genre) throws IOException {
		
//		List<Prod_Books> book_list = Prod_Crawling.getc_Datas();
////		 크롤링된 데이터를 데이터베이스에 저장합니다.
//		prodService.saveBooks(book_list);

		// 키밸류라 생각하면 된다. 여기서 설정한 Prod_Books가 html에서 불리는용, book_list는 여기의 값(데이터 지우신듯
//		model.addAttribute("C_Books", prodService.getAllBooks());
		model.addAttribute("C_Books", prodService.getSearchBooks(kw));
		model.addAttribute("kw", kw);
		model.addAttribute("b_genre", genre);
		model.addAttribute("l_avg", prodService.getAvg_list());
		
		
		// 크롤링된 데이터 그대로 출력 
//		model.addAttribute("C_Books", book_list);
//		System.out.println("모델값");
//		System.out.println(model);
		return "prod/prod_list";
	}
	
	// 제품 상세보기.
	// @PathVariable은 url에 있는 변수 인식하는거.
	@GetMapping("/detail/{book_id}")
	public String prod_book(Model model, @PathVariable("book_id") Integer book_id) {
		// 책아이디 건네주기
		Prod_Books book = prodService.getProdBooks(book_id);
		model.addAttribute("book", book);
		// 평균 별점 보여주기
	    model.addAttribute("score_avg", prodService.getAvgScoreByBookId(book_id));
		// 리뷰 보여주기
		model.addAttribute("r_list", prodService.getReview_List(book_id));
		// 답글 보여주기
		model.addAttribute("a_list", prodService.getAnswer_List());
		// 투표자 수 보여주기
		model.addAttribute("v_list", prodService.getVoters(book_id));
		
		
		return "prod/prod_detail";
	}
	
	// 별점 넣기
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/detail/score/{b_id}")
	public String setScore(@PathVariable("b_id") Integer id, Principal pc, 
			@RequestParam("i_score") Integer score) {
		String user = prodService.getUser(pc.getName());
		System.out.println("스코어 값 " + score);
		
		// 이전에 별점을 줬는지 여부 확인
		boolean b_score = prodService.g_score(id, user);
		// 트루일때(이미 줬을시)
	    if (b_score) {
	    	System.out.println("###########이미 별점 줌");
	    	prodService.update_score(id, user, score);
	        return String.format("redirect:/prod/detail/%s", id);
	    }
	    
	    // 별점 넣기
		prodService.set_score(id, user, score);
		return String.format("redirect:/prod/detail/%s", id);
	}
	
	// 리뷰 쓰기
	// PreAuthorize는 로그인 여부 확인. 로그인 해야만 사용할 수 있음.
	// authenticated는 인증 된 사용자란다.
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/detail/write_review/{r_id}")
	// Principal은 스프링시큐리티 쓸떄 쓰인다나. 사용자 관련인거같음
	public String write_review(Model model,@PathVariable("r_id") Integer id, 
			@RequestParam("w_content") String content,
			Principal pc) {
		// 리뷰에 유저 명 넣기
		// user = 로그인할떄 친 아이디값. 즉 asdf@naver값이 들어온다.
		String user = prodService.getUserName(pc.getName());
		System.out.println("####################컨트롤러 유저 값 확인 " + user);
		prodService.Write_Review(content, id, user);
		return String.format("redirect:/prod/detail/%s", id);
	}
	
	// 리뷰 답글 쓰기
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/detail/write_answer/{b_id}/{a_id}")
	public String write_answer(Model model, @PathVariable("b_id") Integer b_id, 
			@PathVariable("a_id") Prod_d_Review id, @RequestParam("a_content") String content,
			Principal pc) {
		// 답글에 유저 명 넣기
		String user = prodService.getUserName(pc.getName());
		prodService.Write_Answer(id, content, user);
		return String.format("redirect:/prod/detail/%s", b_id);
	}
}