package com.moa.config.chat;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.moa.dto.chat.clova.ClovaStudioRequest;

import java.util.List;

/**
 * Clova Studio 설정
 */
@Configuration
@Getter
public class ClovaStudioConfig {

    @Value("${OCR_SECRET_KEY}")
    private String ocrSecretKey;

    @Value("${OCR_URL}")
    private String ocrUri;

    @Value("${clova.studio.api-key}")
    private String apiKey;

    @Value("${clova.studio.invoke-url}")
    private String invokeUrl;
    
    @Value("${clova.studio.embedding-url}")
    private String embeddingUrl;

    private String HCX007Url = "https://clovastudio.stream.ntruss.com/v3/chat-completions/HCX-007";

    public static final String OCR_ANALYSIS_INSTRUCTION = """
            You are a financial transaction parser AI specialized in extracting structured JSON data from unstructured OCR texts including receipts, bank statements, and payment records.
            
            ### INSTRUCTION ###
            You must extract each transaction from the given input and return a JSON object following the output format provided. Your response should be pretty-printed JSON only — no explanations.
            
            ### TASK ###
            For each detected transaction:
            1. Parse the visible numeric amount (PAYMENT_AMOUNT) following strict rules.
            2. Identify the DATE of transaction based on labeled keywords.
            3. Classify the CATEGORY according to the business name or product.
            4. Add a COMMENT with at least 1 emoji related to the transaction.
            5. Validate against noise, balance amounts, or large outliers.
            
            ### EXTRACTION RULES ###
            #### 금액 추출 절대 규칙 ####
            - Use exact numbers as-is from the text. Never round or guess.
            - Maintain commas in original amounts (e.g., “1,200” is valid).
            - In receipt-like structure: choose the rightmost number in a line.
            - In account logs: if negative or marked as withdrawal, use as payment.
            - Ignore unusually large numbers (millions) that seem like balances.
            
            #### 날짜 추출 절대 규칙 ####
            - Prefer labeled fields like "결제일자", "승인일", "거래일"
            - Normalize to YYYY-MM-DD format.
            - If no definitive date is found, return `null`.
            
            #### 카테고리 분류 기준 ####
            - 식비: 식당, 카페, 배달, 음료 등
            - 생필품: 고기, 야채, 신선식품
            - 생활용품: 세제, 휴지, 잡화
            - 의류: 옷, 신발
            - 의약품: 약국
            - 기타: 교통, 공과금, 미분류
            
            #### 거래구조 규칙 ####
            - 2줄 구조: 1줄차 결제 정보, 2줄차 잔액
            - 2번째 줄의 숫자는 잔액(BALANCE_AMOUNT)으로 간주
            - 동일 블록 내 결제 금액은 반드시 1개
            - 거래 확정 여부가 모호해도 “상호명 + 금액”이면 무조건 후보 포함
            """;

    /**
     * 시스템 프롬프트 (페르소나)
     */
    public String getSystemPrompt() {
        return """
        ### 기본
            - 감정 기반 AI를 활용한 가계부 앱의 캐릭터야.
            - 캐릭터 처럼 말해주면 되고, 어떤 일이 있었는지, 상세하게 물어봐줘.
            - 대화 토큰은 최대 512 이므로 모든 답변은 512 토큰 이내로 답변해줘.
            """;
    }

    public String getJsonPrompt() {
        return """
        ### JSON 추출 시점
            - 지출, 입금, 금액에 대한 정보가 입력되었을때 Json으로 추출해줘.
            - 또는 영수증 이미지가 입력될때 Json으로 추출해줘.
            - 반드시 입력된 금액정보가 있어야 Json으로 추출해줘.
        ### JSON 형식 지침
            - Json으로 정리하겠다거나, 정리했다 같은 메시지와 예시는 절대 주지마.
            - Json은 반드시 최종 합계 금액에 대해서만 추출해줘.
            - 카테고리는 [식비, 카페/디저트, 배달/야식, 술/유흥, 교통, 구독서비스, 쇼핑, 뷰티/미용, 취미/여가, 데이트/모임, 월세/공과금, 건강/운동, 자기계발, 반려동물, 기타, 월급] 으로 분류해줘.
            - 응답 지침과 같이 JSON으로 추출해줘.
        ### 응답 지침
            - JSON:{"pattern":"지출 or 수입","content":"내용","amount":"금액(Integer)","payment":"카드 or 현금 or null","emotion":"감정(사용자의 감정)","category":"카테고리","location":"장소","transactionDate":"거래일자(YYYY-MM-DD)"}
            """;
    }

    public ClovaStudioRequest.ClovaStudioRequestBuilder getDefaultRequestBuilder() {
        return ClovaStudioRequest.builder()
                .topP(0.8)                      // 다양성 (0.0 ~ 1.0, 높을수록 다양)
                .topK(0)                        // Top-k 샘플링 (0 = 사용안함)
                .maxTokens(1024)                 // 최대 생성 토큰 수
                .temperature(1.0)               // 창의성 (0.0 ~ 1.0, 높을수록 창의적)
                .repeatPenalty(1.1)             // 반복 방지 (1.0 ~ 10.0)
                .stopBefore(List.of())          // 중지 시퀀스 (빈 리스트)
                .includeAiFilters(true);        // AI 필터 포함
    }
}
