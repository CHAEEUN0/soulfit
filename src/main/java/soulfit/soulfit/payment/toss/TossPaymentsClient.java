package soulfit.soulfit.payment.toss;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final RestTemplate restTemplate; // 또는 WebClient 사용
    private final String secretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    Logger logger = LoggerFactory.getLogger(TossPaymentsClient.class);

    public TossApproveResponse approvePayment(String paymentKey, String orderId, int amount) {

        logger.info("approvePayment ARGS : (pk : "+paymentKey+", o_id : "+orderId+", amount : "+amount+")");
        String encryptedSecretKey = encryptSecretKey(secretKey);
        logger.info("encrypted secret key : " + encryptedSecretKey);

        // 1. Headers 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", encryptedSecretKey); // 'Authorization' 헤더에 암호화된 시크릿 키 설정

        // 2. Request Body 설정
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("orderId", orderId);
        requestBody.put("amount", amount);
        requestBody.put("paymentKey", paymentKey);

        // 3. HttpEntity 생성 (헤더와 바디 포함)
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 4. POST 요청 보내기
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        try {
            return restTemplate.postForObject(url, entity, TossApproveResponse.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // API로부터 받은 구체적인 에러 응답을 로그로 남깁니다.
            logger.error("Toss Payments API error: Status Code: {}, Response Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null; // 또는 적절한 에러 처리 로직
        } catch (Exception e) {
            // 그 외 일반적인 예외 처리
            logger.error("Error during Toss Payments confirmation: " + e.getMessage(), e);
            return null; // 또는 적절한 에러 처리 로직
        }

        // Toss API 호출 로직 구현
        // 토큰, 헤더 설정 후 POST 요청으로 Toss 결제 승인 API 호출
        // 응답을 TossApproveResponse로 매핑
//        return restTemplate.postForObject(
//                "https://api.tosspayments.com/v1/payments/confirm",
//                new TossApproveRequest(paymentKey, orderId, amount),
//                TossApproveResponse.class
//        );
    }

    public String encryptSecretKey(String secretKey) {
        // secretKey + ':' 부분을 바이트 배열로 변환
        String combinedString = secretKey + ":";
        byte[] bytesToEncode = combinedString.getBytes();

        // Base64 인코딩 수행
        String encodedString = Base64.getEncoder().encodeToString(bytesToEncode);

        // "Basic " 접두사 추가

        return "Basic " + encodedString;
    }





}

