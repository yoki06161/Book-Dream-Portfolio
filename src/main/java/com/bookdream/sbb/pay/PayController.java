package com.bookdream.sbb.pay;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import com.bookdream.sbb.user.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.*;

// 아임포트는 REST API - 이걸 사용하면 html페이지를 반환할 수 없으므로, order 페이지는 분리
@RestController
public class PayController {
	// 결제 검증 서비스
	private IamportClient iamportClient;
	// 결제 후 DB에 결제/주문정보 저장
	private final RestTemplate restTemplate;
	private final PayService payService;
	private final OrdersService ordersService;
	
	@Autowired
    public PayController(RestTemplate restTemplate, PayService payService, OrdersService ordersService) {
        this.restTemplate = restTemplate;
        this.payService = payService;
        this.ordersService = ordersService;
    }
	
    @Value("${imp.api.key}")
    private String apiKey;
 
    @Value("${imp.api.secretkey}")
    private String secretKey;
    
    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey,true);
    }
    
    @PostMapping("/payment/validation/{imp_uid}")
    public IamportResponse<Payment> validateIamport(@PathVariable("imp_uid") String pay_id) throws IamportResponseException, IOException {
    	IamportResponse<Payment> payment = iamportClient.paymentByImpUid(pay_id);
        String name = payment.getResponse().getBuyerName();
        String address = payment.getResponse().getBuyerAddr();
        String phone = payment.getResponse().getBuyerTel();
        String post_code = payment.getResponse().getBuyerPostcode();

        // 금액을 포맷하는 부분
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.KOREA);
        String formattedAmount = currencyFormatter.format(payment.getResponse().getAmount());

        // "원" 단위를 추가해서 total_price를 저장
        String total_price = formattedAmount.replace("₩", "") + "원";

        // 결제 테이블에 값 저장
     	payService.savePays(pay_id, name,phone,address,post_code,total_price);
     	
        return payment;
    }
    
    @PostMapping("/payment/updatePay")
    public ResponseEntity<String> payAdd(@RequestBody Map<String, Object> payData) {
        // payData에서 올바른 키를 사용하여 값을 가져옵니다.
        String pay_id = (String) payData.get("imp_uid");
        String pw = (String) payData.get("pw");
        String request = (String) payData.get("options");

        try {
            payService.updatePaysById(pay_id, pw, request);
            return ResponseEntity.ok("Payment updated successfully");
        } catch (Exception e) {
            // 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    

    @PostMapping("/payment/getToken")
    public ResponseEntity<String> getToken(@RequestBody String body) {
        String apiUrl = "https://api.iamport.kr/users/getToken";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    @PostMapping("/payment/cancel/{imp_uid}")
    public ResponseEntity<String> refundRequest(@RequestBody String body, @PathVariable("imp_uid") String pay_id) {
        try {
            // 토큰 요청
            String tokenBody = "{\"imp_key\":\"" + apiKey + "\", \"imp_secret\":\"" + secretKey + "\"}";
            ResponseEntity<String> tokenResponse = getToken(tokenBody);

            // 토큰 추출
            String token = extractToken(tokenResponse.getBody());

            // 결제 취소 요청
            String apiUrl = "https://api.iamport.kr/payments/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);

            HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);
            
            //결제 테이블에서 데이터 삭제(여기까진 됨)
            payService.deletePayByPayId(pay_id);
            
            //주문 테이블에서 데이터 삭제
            ordersService.deleteOrdersByPayId(pay_id);
            
            return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    private String extractToken(String responseBody) {
        // responseBody에서 토큰 값 추출 로직 추가
        // JSON 파싱하여 access_token 값 반환
        // 예: {"response":{"access_token":"YOUR_ACCESS_TOKEN",...}}
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("response").path("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse token from response", e);
        }
    }
}