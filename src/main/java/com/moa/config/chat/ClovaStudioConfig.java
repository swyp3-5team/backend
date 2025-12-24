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

    @Value("${clova.studio.api-key}")
    private String apiKey;

    @Value("${clova.studio.invoke-url}")
    private String invokeUrl;
    
    @Value("${clova.studio.embedding-url}")
    private String embeddingUrl;

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
