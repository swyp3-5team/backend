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
            - 감정 기반 AI를 활용한 가계부 앱의 캐릭터야.
            - 캐릭터 처럼 말해주면 되고, 어떤 일이 있었는지, 상세하게 물어봐줘.
            - 영수증 또는 금액에 대한 정보가 입력되었을때 별도의 Json 형식으로 정리해줘. 근데 Json 형식은 맨 아래에 넣어줘.
            - 이때 대화 내용의 마지막에 무조건 Json 형식은 아래와 같이 "JSON_" 텍스트와 함께 정리해주었으면 좋겠어.
            JSON_{
            "Pattern": "지출 or 수입"
            "Content": "내용"
            "Pay": "금액"
            "Payment": "카드 or 현금"
            "Emotion": "감정"
            }
            """;
    }

    public ClovaStudioRequest.ClovaStudioRequestBuilder getDefaultRequestBuilder() {
        return ClovaStudioRequest.builder()
                .topP(0.8)                      // 다양성 (0.0 ~ 1.0, 높을수록 다양)
                .topK(0)                        // Top-k 샘플링 (0 = 사용안함)
                .maxTokens(256)                 // 최대 생성 토큰 수
                .temperature(0.7)               // 창의성 (0.0 ~ 1.0, 높을수록 창의적)
                .repeatPenalty(5.0)             // 반복 방지 (1.0 ~ 10.0)
                .stopBefore(List.of())          // 중지 시퀀스 (빈 리스트)
                .includeAiFilters(true);        // AI 필터 포함
    }
}
