package com.bookdream.sbb.prod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.bookdream.sbb.event.Event;
import com.bookdream.sbb.prod_repo.Prod_Books;
import com.bookdream.sbb.prod_repo.Prod_BooksRepository;
import com.bookdream.sbb.prod_repo.Prod_RArepository;
import com.bookdream.sbb.prod_repo.Prod_RErepository;
import com.bookdream.sbb.prod_repo.Prod_Score;
import com.bookdream.sbb.prod_repo.Prod_ScoreRepository;
import com.bookdream.sbb.prod_repo.Prod_d_Answer;
import com.bookdream.sbb.prod_repo.Prod_d_Review;
import com.bookdream.sbb.user.Member;
import com.bookdream.sbb.user.MemberRepository;
import com.bookdream.sbb.user.SiteUser;
import com.bookdream.sbb.user.UserRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;

// final쓸때 씀
@RequiredArgsConstructor
@Service
public class Prod_Service {
	@Autowired
	private final Prod_BooksRepository prodRepository;
	private final Prod_RErepository re_repo;
	private final Prod_RArepository ra_repo;
	private final Prod_ScoreRepository sco_repo;

	// 두목님꺼. 일반로그인
	private final UserRepository user_repo;
	private final MemberRepository mem_repo;

	// 모든 책 리스트 갖고오기. 안쓰이긴 하는데 혹시 모르니까.
	public List<Prod_Books> getAllBooks() {
		// prodRepository를 이용해 데이터 베이스에 저장된 모든 책을 찾음
		return prodRepository.findAll();
	}

	// 검색된 책들만 갖고 오기
	public List<Prod_Books> getSearchBooks(String kw) {
		// 밑의 search메소드에 kw값 넣고 꺼내기
		Specification<Prod_Books> spec = search(kw);
		return prodRepository.findAll(spec);
	}

	// book_list의 책들을 모두 db에 저장함
	void saveBooks(List<Prod_Books> book_list) {		
		prodRepository.saveAll(book_list);
	}

	// 제품 상세보기. 받은 id에 따라 db에서 책 정보 조회
	public Prod_Books getProdBooks(Integer book_id) {
		// Optional임시 데이터 타입인듯. 무슨 데이터 타입이든 받아들이는
		// select * from prodRepository where id = book_id라 생각하자.
		Optional<Prod_Books> opb = prodRepository.findById(book_id);
		return opb.get();
	}

	// DB 에서 정보 조회함
	public List<Prod_Books> getAllProdBooks(List<Integer> bookIds) {
		// bookIds에 해당하는 모든 책 정보를 조회
		return prodRepository.findAllById(bookIds);
	}

	// 검색
	// Specification은 요구사항을 명확히 설정?
	private Specification<Prod_Books> search(String kw) {
		return new Specification<>() {
			private static final long SerialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Prod_Books> books, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true); // 중복을 제거
				// like로 검색
				return cb.or(cb.like(books.get("book_title"), "%" + kw + "%"),	// 제목
						cb.like(books.get("book_writer"), "%" + kw + "%"));	// 저자
			}
		};
	}

	// ###################리뷰
	// 리뷰 리스트 갖고오기. 책에 맞는 리뷰갖고오기
	public List<Prod_d_Review> getReview_List(Integer book_id) {
		List<Prod_d_Review> r_list = re_repo.findByBook(book_id);
		return r_list;
	}

	// 리뷰 쓰기
	public void Write_Review(String review, Integer book_id, String user) {
		Prod_d_Review pr = new Prod_d_Review();
		pr.setReview(review);
		pr.setBook(book_id);
		pr.setTime(LocalDate.now());
		pr.setUser(user);

		re_repo.save(pr);
	}

	// 리뷰 답글 갖고오기.
	public List<Prod_d_Answer> getAnswer_List() {
		List<Prod_d_Answer> a_list = ra_repo.findAll();
		return a_list;
	}

	// 리뷰 답글 쓰기
	public void Write_Answer(Prod_d_Review review_id, String content, String user) {
		Prod_d_Answer pa = new Prod_d_Answer();
		pa.setAnswer(content);
		pa.setTime(LocalDate.now());
		pa.setReview(review_id);
		pa.setUser(user);

		ra_repo.save(pa);
	}

	// 리뷰 및 답글에 로그인한 사용자 닉네임 넣기.
	public String getUserName(String user) {
		// 로그인할떄 email값이 들어와서 email = user인 값을 db로 찾는다. where email = user 
		Optional<SiteUser> user_email = user_repo.findByEmail(user);
		// 위의건 그냥 로그인. 밑의건 구글, 카카오 로그인.
		Member mem_email = mem_repo.findByLoginId(user);
		//    	System.out.println("###########################서비스 user값 " + user);
		//    	System.out.println("###########################서비스 mem_email값 " + mem_email);

		// ispresent는 optional에서만 되서 mem_email은 못씀.
		if(user_email.isPresent()) {
			// username.get()이라고만 쓰면 com.bookdream.sbb.user.SiteUser@67b61fcd식으로 경로만 뜬다.
			// .get()모두.get원하는 칼럼명(). get원하는 칼럼명()을 해야 그 칼럼의 값을 갖고옴.
			// SiteUser의 username칼럼 자체가 string 형태라 stieuser user_name = 으로 못불러온다.
			String user_name = user_email.get().getUsername();

			//    	    System.out.println("###########################서비스 user_name값" + user_name);
			//        	System.out.println("###########################서비스 user_email.get()값" + user_email.get());

			return user_name;

		} else if(mem_email != null) {
			String mem_name = mem_email.getName();
			//    		System.out.println("###########################서비스 mem_name값" + mem_name);

			return mem_name;

		} else {
			// 이제 나올리 없음.
			return "익명";
		}
	}

	 // 별점 넣기 위한 이메일 불러오기
    public String getUser(String user) {
		Optional<SiteUser> siteUser = user_repo.findByEmail(user);
		Member mem_email = mem_repo.findByLoginId(user);
//		System.out.println("!!!!!!!!!!!!!!!일반 유저 이메일 " + siteUser);
//		System.out.println("!!!!!!!!!!!!!!!카카오 유저 이메일 " + mem_email);
		
		if(siteUser.isPresent()) {
			String user_name = siteUser.get().getEmail();
//			System.out.println("!!!!!!!!!!!!!!!리턴 일반 유저 이메일 " + user_name);
    		return user_name;
		} else if(mem_email != null) {
    		String mem_name = mem_email.getLoginId();
//    		System.out.println("!!!!!!!!!!!!!!!리턴 카카오 유저 이메일 " + mem_name);
    		return mem_name;
    	} else {
    		// 이제 나올리 없음.
    		return "익명";
    	}
	}
    
    // 별점 넣기
    public void set_score(Integer book, String user, Integer score) {
    	Prod_Score sc = new Prod_Score();
    	sc.setBook(book);
    	sc.setUser(user);
    	sc.setScore(score);
    	
    	sco_repo.save(sc);
	}
    
    // 책에 따른 별점 갖고오기.
    public String getAvgScoreByBookId(Integer book_id) {
    	Double score_avg = sco_repo.findAvgScoreBybook(book_id);
    	// 별점이 있을시
    	if(score_avg != null) {
			// 소수점 한자리수까지 출력.
			String score = String.format("%.2f", score_avg);
			return score;
    	}
		// 별점평가가 아직 없을시 0.0리턴
    	return "0.0";
    }
    
    // 리스트에 별점 평균 리스트 보내기
	public List<Object[]> getAvg_list() {
		List<Object[]> avg_list = sco_repo.findAvgScore();
		
		for (Object[] obj : avg_list) {
			BigDecimal avg_score = (BigDecimal) obj[1];
			// 반올림하여 소수점 둘째 자리까지 표시
			BigDecimal roundedScore = avg_score.setScale(2, RoundingMode.HALF_UP);
			// BigDecimal을 문자열로 포맷팅하여 소수점 둘째 자리까지 출력
			obj[1] = String.format("%.2f", roundedScore);
        }
		
		return avg_list;
	}
	
	// 별점 리스트. 투표자들 명수 출력용.
	public List<Prod_Score> getVoters(Integer b_id) {
		return sco_repo.findBybook(b_id);
	}
	
	// 사용자가 이미 별점을 준 기록이 있는지 확인
    public boolean g_score(Integer book, String user) {
        Prod_Score score = sco_repo.findByBookAndUser(book, user);
        // 처음 별점 넣을시 score값은 널이고, 아닐시 com.bookdream.sbb.prod_repo.Prod_Score@3e439783식으로 뜸.
        System.out.println("==========스코어값 " + score);
        
        // 만약 스코어 값이 널이 아니면 트루 반환, 널일시 폴즈 반환
        return score != null;
    }
    
    // 별점 수정
    public void update_score(Integer book, String user, Integer score) {
        Prod_Score p_score = sco_repo.findByBookAndUser(book, user);
        // 처음 별점 넣을시 score값은 널이고, 아닐시 com.bookdream.sbb.prod_repo.Prod_Score@3e439783식으로 뜸.
        System.out.println("==========pppppppp스코어값 " + p_score);
        
    	p_score.setScore(score);
    	
    	sco_repo.save(p_score);
    }

	//main 화면 작업 -이준희
	//랜덤 책 추천
	public List<Prod_Books> getRecommendedBooks() {
		List<Prod_Books> allBooks = StreamSupport
				.stream(prodRepository.findAll().spliterator(), false)
				.collect(Collectors.toList());

		Collections.shuffle(allBooks);
		return allBooks.stream().limit(4).collect(Collectors.toList());
	}

	public List<Prod_d_Review> getAllReviews() {
		return re_repo.findAll();
	}

	public void deleteReview(int id) {
		this.re_repo.deleteById(id);
	}

	public Prod_d_Review getReview(int id) {
		Optional<Prod_d_Review> review = this.re_repo.findById(id);
		return review.get();
	}
	
	public void deleteAnswer(int id) {
		this.ra_repo.deleteById(id);
	}

	public Prod_d_Answer getAnswer(int id) {
		Optional<Prod_d_Answer> answer = this.ra_repo.findById(id);
		return answer.get();
	}

}

