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

    private String HCX007Url = "https://clovastudio.stream.ntruss.com/v3/chat-completions/HCX-007";

    public static final String OCR_ANALYSIS_INSTRUCTION = """
            너는 모든 형태의 지출 내역을 분석하여
            정확한 JSON 데이터를 생성하는 엔진이다.
            
            ────────────────────────────────
            [금액 추출 절대 규칙]
            ────────────────────────────────
            1. 숫자 왜곡 금지
               - 영수증·거래내역에 적힌 숫자를 단 1이라도 수정, 반올림, 추측하지 마라.
               - 눈에 보이는 숫자를 그대로 사용하라.
            
            2. 콤마 유지
               - 원문에 콤마(,)가 포함된 금액은 반드시 콤마를 유지하라.
            
            3. 우선순위 판별
               - [영수증 형태]
                 한 줄에 숫자가 여러 개라면 가장 오른쪽에 있는 ‘총액’을 선택한다.
               - [계좌 내역 형태]
                 금액이 하나뿐이고 마이너스(-) 기호가 있거나 ‘출금’ 표시가 있다면
                 해당 숫자를 결제 금액으로 사용한다.
            
            4. 잔액 필터링
               - 결제 금액보다 현저히 큰 숫자(백만 단위 이상)는
                 잔액 또는 누적 금액으로 판단하고 절대 사용하지 마라.
            
            ────────────────────────────────
            [카테고리 분류 가이드]
            ────────────────────────────────
            - 식비: 식당, 카페, 배달앱, 가공식품, 과자, 음료
            - 생필품: 고기, 채소, 우유, 달걀 등 신선식품
            - 생활용품: 쇼핑백, 양복커버, 세제, 휴지 등 잡화
            - 의류: 옷, 신발, 속옷
            - 의약품: 약국 및 일반 의약품
            - 기타: 교통비, 서비스 이용료 등 기타 지출
            
            ────────────────────────────────
            [날짜 추출 절대 규칙]
            ────────────────────────────────
            1. 우선순위
               - ‘결제일자’, ‘결제일’, ‘승인일’, ‘거래일’과 같이
                 명시적 라벨이 붙은 날짜가 있으면
                 다른 모든 날짜 후보를 무시하고 해당 날짜를 사용한다.
            
            2. 허용 날짜 형식
               - YYYY-MM-DD / YYYY.MM.DD / YYYY/MM/DD
               - YY년 M월 D일 (YY는 2000~2099로 해석)
               - M.D / M/D
                 (이 경우 연도는 결제일자 라벨이 있는 연도를 사용)
            
            3. 정규화
               - 최종 출력은 반드시 YYYY-MM-DD 형식으로 변환한다.
            
            4. 예외 처리
               - 확정 가능한 날짜가 없을 경우
                 transactionDate는 null로 출력한다.
               - 추측으로 날짜를 생성하지 마라.
            
            ────────────────────────────────
            [코멘트 출력 규칙]
            ────────────────────────────────
            - comment 필드는 반드시 기본 이모지를 1개 이상 포함해야 한다.
            
            ────────────────────────────────
            [출력 형식 규칙]
            ────────────────────────────────
            1. 응답의 JSON 데이터(content)는
               줄바꿈(\\n)을 포함한 Pretty-Print 형식으로 출력하라.
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
