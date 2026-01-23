package com.moa.service.chat;

import com.moa.config.chat.ClovaStudioConfig;
import com.moa.dto.*;
import com.moa.dto.chat.ChatHistoryResponse;
import com.moa.dto.chat.ReceiptResponse;
import com.moa.dto.chat.clova.ClovaStudioRequest;
import com.moa.entity.*;
import com.moa.exception.InvalidImageException;
import com.moa.exception.UserNotFoundException;
import com.moa.reponse.AiReceiptResponse;
import com.moa.repository.AiChattingLogRepository;
import com.moa.repository.UserRepository;
import com.moa.service.OcrService;
import com.moa.service.UpstageLLMResponse;
import com.moa.service.UpstageStudioService;
import com.moa.service.chat.clova.ClovaStudioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.moa.util.LocalDateParser.parseLocalDate;

/**
 * 채팅 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ClovaStudioService clovaStudioService;
    private final UpstageStudioService upstageStudioService;
    private final AiChattingLogRepository chattingLogRepository;
    private final UserRepository userRepository;
    private final ClovaStudioConfig clovaConfig;
    private final OcrService ocrService;

    @Transactional
    public ReceiptResponse sendMessage(Long userId, String userMessage, String mode, MultipartFile image) {
        try {
            // 1. 사용자 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.info("채팅 메시지 전송 - userId: {}, message: {}", userId, userMessage);
            StringBuilder ragContext = null;
            List<AiChattingLog> similarChats = new ArrayList<>();
            // 2. 사용자 메시지 임베딩 벡터 생성 (RAG용)
            if (userMessage != null) {
                List<Double> userEmbedding = clovaStudioService.embedText(userMessage);
                String embeddingVectorStr = convertEmbeddingToString(userEmbedding);
                // 3. 사용자 메시지 저장 (임베딩 벡터 포함)
                AiChattingLog userLog = AiChattingLog.builder()
                        .user(user)
                        .chatContent(userMessage)
                        .chatType("USER")
                        .embeddingVector(embeddingVectorStr)
                        .build();
                userLog = chattingLogRepository.save(userLog);

                // 4. RAG: 과거 대화 중 유사한 내용 검색 (벡터 유사도 기반)

                if (userEmbedding != null && !userEmbedding.isEmpty()) {
                    log.info("RAG 벡터 검색 시작 - userId: {}", userId);
                    similarChats = chattingLogRepository.findSimilarChats(userId, embeddingVectorStr, 5);
                    log.info("유사한 과거 대화 {}개 발견", similarChats.size());
                }
                // 5: 유사한 과거 대화가 있으면 컨텍스트에 추가

                if (!similarChats.isEmpty()) {
                    ragContext = new StringBuilder("### 참고: 과거 유사한 대화 내역\n");
                    for (AiChattingLog chat : similarChats) {
                        if (!chat.getChattingId().equals(userLog.getChattingId())) {
                            ragContext.append("- ").append(chat.getChatContent()).append("\n");
                        }
                    }
                    log.info("유사 대화 내역 : {}", ragContext.toString());
                }
            }

            // 6. 메시지 리스트 구성
            List<ClovaStudioRequest.Message> messages = new ArrayList<>();

            String systemPrompt = clovaConfig.getSystemPrompt();
            log.info("RAG CONTEXT: {}", ragContext);
            // 시스템 프롬프트 추가
            messages.add(ClovaStudioRequest.Message.builder()
                    .role("system")
                    .content(ragContext != null ? ragContext.toString() + systemPrompt : systemPrompt)
                    .build());

            // 현재 사용자 메시지 추가 (이미지 포함 여부에 따라 처리)
            messages.add(buildUserMessage(userMessage, image));

            log.info("Clova Studio API 호출 - 메시지 개수: {}, RAG 활성화: {}",
                    messages.size(), !similarChats.isEmpty());

            // 7. Clova Studio API 호출
            String aiResponse = clovaStudioService.sendMessage(messages);

//            // 8. JSON 파싱 (거래내역 추출)
//            ChatResponse cleanReceiptResponse = extractTransactionGroupInfo(aiResponse);
//            TransactionGroupInfo transactionGroupInfo = cleanReceiptResponse != null ? cleanReceiptResponse.getTransactionInfo() : null;

            // 9. AI 응답 임베딩 벡터 생성 및 저장
            List<Double> aiEmbedding = clovaStudioService.embedText(aiResponse);
            String aiEmbeddingVectorStr = convertEmbeddingToString(aiEmbedding);

            AiChattingLog assistantLog = AiChattingLog.builder()
                    .user(user)
                    .chatContent(aiResponse)
                    .chatType("ASSISTANT")
//                    .emotion(transactionInfo != null ? transactionInfo.getEmotion().name() : null)
                    .embeddingVector(aiEmbeddingVectorStr)
                    .build();
            assistantLog = chattingLogRepository.save(assistantLog);

            log.info("채팅 응답 완료 - chattingId: {}, 거래내역: {}");

            return ReceiptResponse.builder()
                    .message(aiResponse)
                    .build();

        } catch (Exception e) {
            log.error("채팅 메시지 전송 실패", e);
            throw new RuntimeException("채팅 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public AiReceiptResponse sendReceiptMessage(Long userId, String userMessage, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("사용자를 찾을 수 없습니다.")
        );

        log.info("영수증 모드 userId : {}", userId);
        String text = null;
        String OcrText = null;
        if (image != null) {
            OcrText = ocrService.upstageOcr(image); // OCR text 출력
            log.info("OCR Text : {}", OcrText);
        }
        text = userMessage;

        AiReceiptResponse response = getStructuredOutput(text, OcrText);
        AiTransactionResponse data = response.request();
        String embeddingText = toNaturalText(
                new TransactionGroupInfo(
                        null,
                        data.transactionDate(),
                        data.totalAmount(),
                        data.place(),
                        data.payment(),
                        data.paymentMemo(),
                        data.emotion(),
                        data.transactions().stream().map(
                                tr -> {
                                    return new TransactionInfo(
                                            null,
                                            tr.name(),
                                            tr.amount(),
                                            null,
                                            tr.categoryName(),
                                            "EXPENSE"
                                    );
                                }).toList()

                )
        );

        String strEmbedding = convertEmbeddingToString(clovaStudioService.embedText(embeddingText));
        log.info("자연어 생성 및 임베딩 생성 \nNatural String: ${} \nEmbedding: ${}", embeddingText, strEmbedding);
        AiChattingLog userLog = AiChattingLog.builder()
                .user(user)
                .chatContent(embeddingText)
                .chatType("USER")
                .embeddingVector(strEmbedding)
                .build();
        chattingLogRepository.save(userLog);
        return response;
    }

    private AiReceiptResponse getStructuredOutput(String text, String OcrText) {
        //프롬프트 구성
        List<UpstageLLMRequest.Message> prompts = new ArrayList<>();
        String OCR_ANALYSIS_INSTRUCTION = String.format(ClovaStudioConfig.OCR_ANALYSIS_INSTRUCTION, LocalDate.now().toString());
//        Hcx007RequestDto.Message systemPrompt = Hcx007RequestDto.Message.builder()
//                .role("system")
//                .content(OCR_ANALYSIS_INSTRUCTION)
//                .build();


        // 시스템 프롬프트
        prompts.add(
                UpstageLLMRequest.Message.builder()
                        .role("system")
                        .content(OCR_ANALYSIS_INSTRUCTION)
                        .build()
        );
        StringBuilder userContent = new StringBuilder();
        if (text != null && !text.isBlank()) {
            userContent
                    .append(text)
                    .append("\n\n");
        }
        if (OcrText != null && !OcrText.isBlank()) {
            userContent
                    .append("거래 내역\n")
                    .append(OcrText)
                    .append("\n\n");
        }
        // 유저 입력
        prompts.add(
                UpstageLLMRequest.Message.builder()
                        .role("user")
                        .content(userContent.toString())
                        .build()
        );
        for (UpstageLLMRequest.Message message : prompts) {
            log.info("[{}] content:\n{}", message.getRole(), message.getContent());
        }

        UpstageLLMRequest.ResponseFormat responseFormat = UpstageLLMRequest.ResponseFormat.createUpstageResponseFormat();
        // 요청 생성
        UpstageLLMRequest upstageLLMRequest = UpstageLLMRequest.builder()
                .model("solar-pro2")
                .messages(prompts)
                .response_format(UpstageLLMRequest.ResponseFormat.builder()
                        .type("json_schema")
                        .json_schema(
                                UpstageLLMRequest.ResponseFormat.JsonSchema.builder()
                                        .name("Receipt")
                                        .strict(true)
                                        .schema(responseFormat.getJson_schema().getSchema())
                                        .build()
                        )
                        .build())
                .build();

        UpstageLLMResponse response = upstageStudioService.sendReceiptMessage(upstageLLMRequest);

        List<UpstageLLMResponse.Item> items = response.items();
        List<TransactionDetailRequest> detailRequests = items.stream().map(item -> {
                    return new TransactionDetailRequest(
                            item.amount(),
                            item.name(),
                            item.category()
                    );
                }
        ).toList();
        return new AiReceiptResponse(
                response.comment(),
                new AiTransactionResponse(
                        response.place(),
                        parseLocalDate(response.transactionDate()),
                        response.payment(),
                        null,
                        detailRequests.stream().mapToLong(
                                TransactionDetailRequest::amount
                        ).sum(),
                        response.emotion(),
                        detailRequests
                )
        );
    }


    private ReceiptResponse extractTransactionGroupInfo(String aiResponse) {
        int beginIdx = -1;
        String upperAiResponse = aiResponse.toLowerCase();
        try {
            int jsonStart = upperAiResponse.indexOf("json");
            if (jsonStart == -1) {
                jsonStart = upperAiResponse.indexOf("{");
                if (jsonStart == -1) {
                    return null;
                } else {
                    beginIdx = "{".length();
                }
            } else {
                beginIdx = "json".length();
            }

            int jsonEnd = upperAiResponse.indexOf("}", jsonStart);
            if (jsonEnd == -1) {
                return null;
            }

            String cleanMessage = aiResponse.substring(0, jsonStart).trim();

            String jsonString = aiResponse.substring(jsonStart + beginIdx, jsonEnd);

            // 간단한 파싱 (정규식 사용)
            String pattern = extractField(jsonString, "pattern");
            String content = extractField(jsonString, "content");
            String amount = extractField(jsonString, "amount");
            String payment = extractField(jsonString, "payment");
            String emotion = extractField(jsonString, "emotion");
            String category = extractField(jsonString, "category");
            String place = extractField(jsonString, "place");
            String transactionDateStr = extractField(jsonString, "transactionDate");

            return null;

        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private String extractField(String json, String fieldName) {
        // 1차 시도: 따옴표 있는 값 추출 - "Pattern": "지출"
        Pattern quotedPattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher quotedMatcher = quotedPattern.matcher(json);
        if (quotedMatcher.find()) {
            return patternText(quotedMatcher.group(1));
        }

        // 2차 시도: 따옴표 없는 값 추출 - "Pattern": 지출
        Pattern unquotedPattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*([^,}\\s]+)");
        Matcher unquotedMatcher = unquotedPattern.matcher(json);
        if (unquotedMatcher.find()) {
            return patternText(unquotedMatcher.group(1).trim());
        }

        return null;
    }

    private String patternText(String value) {
        return value.isEmpty() || "null".equals(value) ? null : value;  // 빈 문자열/null 은 null 반환
    }

    /**
     * 사용자 메시지 생성 (텍스트만 또는 텍스트+이미지)
     */
    private ClovaStudioRequest.Message buildUserMessage(String text, MultipartFile image) {
        if (image == null) {
            // 텍스트만 전송 (기존 방식 - 역호환성 유지)
            return ClovaStudioRequest.Message.builder()
                    .role("user")
                    .content(text)
                    .build();

        } else {
            // 텍스트 + 이미지 (멀티모달)
            List<ClovaStudioRequest.MessageContentPart> contentParts = new ArrayList<>();
            if (text != null) {
                // 텍스트 파트 추가
                contentParts.add(ClovaStudioRequest.MessageContentPart.builder()
                        .type("text")
                        .text(text)
                        .build());
            }

            // 이미지 파트 추가 (Base64 변환)
            String base64Image = convertImageToBase64(image);
            contentParts.add(ClovaStudioRequest.MessageContentPart.builder()
                    .type("image_url")
                    .dataUri(ClovaStudioRequest.ImageData.builder()
                            .data(base64Image)
                            .build())
                    .build());

            log.info("멀티모달 메시지 생성 완료 - 텍스트: {}, 이미지 크기: {} bytes",
                    text, image.getSize());

            return ClovaStudioRequest.Message.builder()
                    .role("user")
                    .content(contentParts)
                    .build();
        }
    }

    /**
     * 이미지를 Base64로 변환
     */
    private String convertImageToBase64(MultipartFile image) {
        try {
            byte[] imageBytes = image.getBytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = image.getContentType();

            String dataUri = String.format("data:%s;base64,%s", mimeType, base64);

            log.debug("이미지 Base64 변환 완료 - 원본: {} bytes, Base64: {} chars",
                    imageBytes.length, base64.length());

            return dataUri;
        } catch (IOException e) {
            log.error("이미지 Base64 변환 실패: {}", e.getMessage(), e);
            throw new InvalidImageException("이미지 처리 중 오류가 발생했습니다.");
        }
    }

    /*
     * 지출내역을 자연스러운 문장으로 변환
     * */
    public String toNaturalText(TransactionGroupInfo group) {
        List<TransactionInfo> transactions = group.transactionInfoList();
        StringBuilder sb = new StringBuilder();

        // 날짜
        LocalDate date = group.transactionDate();
        sb.append(String.format("%d년 %d월 %d일", date.getYear(), date.getMonthValue(), date.getDayOfMonth()));

        // 장소
        if (group.place() != null && !group.place().isBlank()) {
            sb.append(" ").append(group.place()).append("에서 ");
        } else {
            sb.append("에서 ");
        }

        // 아이템들 나열
        List<String> items = new ArrayList<>();
        for (TransactionInfo t : transactions) {
            String itemStr = String.format("%s(%d원%s)",
                    t.name(),
                    t.amount(),
                    t.categoryName() != null ? ", " + t.categoryName() : ""
            );
            items.add(itemStr);
        }

        sb.append(String.join(", ", items));

        // 결제수단
        if (group.payment() != null) {
            sb.append("을 ").append(mapPayment(group.payment())).append("으로 결제함.");
        } else {
            sb.append("을 결제함.");
        }

        // 감정 표현
        if (group.emotion() != null) {
            sb.append(" ");
            sb.append(TransactionEmotion.parseEmotion(group.emotion()).getNaturalString());
        }

        return sb.toString().trim();
    }

    private String mapPayment(String payment) {
        return switch (payment) {
            case "CARD" -> "카드";
            case "CASH" -> "현금";
            case "TRANSFER" -> "계좌이체";
            default -> payment;
        };
    }


    public List<ChatHistoryResponse> getChatHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AiChattingLog> logs = chattingLogRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId, pageable);

        return logs.getContent().stream()
                .map(ChatHistoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChatHistory(Long userId) {
        chattingLogRepository.deleteByUserUserId(userId);
        log.info("사용자 {}의 대화 히스토리 삭제 완료", userId);
    }

    /**
     * 시간대별 인사 메시지 반환
     */
    public ChatHistoryResponse getGreetingByTime(Long userId) {
        LocalTime now = LocalTime.now();
        CharacterEmotionType emotionType;
        String[] greetingList;

        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(12, 0))) {
            // 06:00~12:00 - 아침
            emotionType = CharacterEmotionType.BASIC;
            greetingList = GreetingByTimeType.MORNING_GREETINGS.getGreetingList();
        } else if (now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(18, 0))) {
            // 12:00~18:00 - 오후
            emotionType = CharacterEmotionType.HAPPY;
            greetingList = GreetingByTimeType.AFTERNOON_GREETINGS.getGreetingList();
        } else if (now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            // 18:00~22:00 - 저녁
            emotionType = CharacterEmotionType.CHEER;
            greetingList = GreetingByTimeType.EVENING_GREETINGS.getGreetingList();
        } else {
            // 22:00~06:00 - 밤
            emotionType = CharacterEmotionType.COMFORT;
            greetingList = GreetingByTimeType.NIGHT_GREETINGS.getGreetingList();
        }

        // 랜덤하게 하나 선택
        Random random = new Random();
        String greetingMessage = greetingList[random.nextInt(greetingList.length)];

        return ChatHistoryResponse.builder()
                .chatType("ASSISTANT")
                .chatContent(greetingMessage)
                .emotion(emotionType.getEmoji())
                .build();
    }

    private String convertEmbeddingToString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            sb.append(embedding.get(i));
            if (i < embedding.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
