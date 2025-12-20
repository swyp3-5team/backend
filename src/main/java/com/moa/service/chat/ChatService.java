package com.moa.service.chat;

import com.moa.config.chat.ClovaStudioConfig;
import com.moa.dto.chat.ChatHistoryResponse;
import com.moa.dto.chat.ChatResponse;
import com.moa.dto.chat.ChatResponse.TransactionInfo;
import com.moa.dto.chat.clova.ClovaStudioRequest;
import com.moa.entity.AiChattingLog;
import com.moa.entity.CharacterEmotionType;
import com.moa.entity.ChatModeType;
import com.moa.entity.GreetingByTimeType;
import com.moa.entity.User;
import com.moa.exception.InvalidImageException;
import com.moa.repository.AiChattingLogRepository;
import com.moa.repository.UserRepository;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 채팅 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ClovaStudioService clovaStudioService;
    private final AiChattingLogRepository chattingLogRepository;
    private final UserRepository userRepository;
    private final ClovaStudioConfig clovaConfig;

    @Transactional
    public ChatResponse sendMessage(Long userId, String userMessage, String mode, MultipartFile image) {
        try {
            // 1. 사용자 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            log.info("채팅 메시지 전송 - userId: {}, message: {}", userId, userMessage);

            // 2. 사용자 메시지 임베딩 벡터 생성 (RAG용)
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
            List<AiChattingLog> similarChats = new ArrayList<>();
            if (userEmbedding != null && !userEmbedding.isEmpty()) {
                log.info("RAG 벡터 검색 시작 - userId: {}", userId);
                similarChats = chattingLogRepository.findSimilarChats(userId, embeddingVectorStr, 5);
                log.info("유사한 과거 대화 {}개 발견", similarChats.size());
            }

            // 5: 유사한 과거 대화가 있으면 컨텍스트에 추가
            StringBuilder ragContext = null;
            if (!similarChats.isEmpty()) {
                ragContext = new StringBuilder("### 참고: 과거 유사한 대화 내역\n");
                for (AiChattingLog chat : similarChats) {
                    if (!chat.getChattingId().equals(userLog.getChattingId())) {
                        ragContext.append("- ").append(chat.getChatContent()).append("\n");
                    }
                }
                log.info("유사 대화 내역 : {}", ragContext.toString());
            }


            // 6. 메시지 리스트 구성
            List<ClovaStudioRequest.Message> messages = new ArrayList<>();

            String systemPrompt = clovaConfig.getSystemPrompt();
            if(mode.equals(ChatModeType.CHAT.getText())) {
                // 대화 모드는 그대로...
            } else if(mode.equals(ChatModeType.RECEIPT.getText())) {
                // 내역 모드는 Json 형식 반환
                systemPrompt = clovaConfig.getSystemPrompt() + clovaConfig.getJsonPrompt();
            }

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

            // 8. JSON 파싱 (거래내역 추출)
            ChatResponse cleanChatResponse = extractTransactionInfo(aiResponse);
            ChatResponse.TransactionInfo transactionInfo = cleanChatResponse != null ? cleanChatResponse.getTransactionInfo() : null;

            // 9. AI 응답 임베딩 벡터 생성 및 저장
            List<Double> aiEmbedding = clovaStudioService.embedText(aiResponse);
            String aiEmbeddingVectorStr = convertEmbeddingToString(aiEmbedding);

            AiChattingLog assistantLog = AiChattingLog.builder()
                    .user(user)
                    .chatContent(aiResponse)
                    .chatType("ASSISTANT")
                    .emotion(transactionInfo != null ? transactionInfo.getEmotion() : null)
                    .embeddingVector(aiEmbeddingVectorStr)
                    .build();
            assistantLog = chattingLogRepository.save(assistantLog);

            log.info("채팅 응답 완료 - chattingId: {}, 거래내역: {}",
                    assistantLog.getChattingId(), transactionInfo != null ? "있음" : "없음");

            return ChatResponse.builder()
                    .message(cleanChatResponse == null ? aiResponse : cleanChatResponse.getMessage())
                    .transactionInfo(transactionInfo)
                    .build();

        } catch (Exception e) {
            log.error("채팅 메시지 전송 실패", e);
            throw new RuntimeException("채팅 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private ChatResponse extractTransactionInfo(String aiResponse) {
        int beginIdx = -1;
        String upperAiResponse = aiResponse.toLowerCase();
        try {
            int jsonStart = upperAiResponse.indexOf("json");
            if (jsonStart == -1) {
                jsonStart = upperAiResponse.indexOf("{");
                if(jsonStart == -1) {
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

            TransactionInfo transactionInfo = ChatResponse.TransactionInfo.builder()
                    .pattern(pattern)
                    .content(content)
                    .amount(TransactionInfo.parseAmount(amount))
                    .payment(payment)
                    .emotion(emotion)
                    .category(category)
                    .place(place)
                    .transactionDate(transactionDateStr != null ? LocalDate.parse(transactionDateStr) : null)
                    .build();

            return ChatResponse.builder()
                    .message(cleanMessage)
                    .transactionInfo(transactionInfo)
                    .build();

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

            // 텍스트 파트 추가
            contentParts.add(ClovaStudioRequest.MessageContentPart.builder()
                    .type("text")
                    .text(text)
                    .build());

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
