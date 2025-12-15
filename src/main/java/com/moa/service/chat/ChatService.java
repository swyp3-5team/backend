package com.moa.service.chat;

import com.moa.config.chat.ClovaStudioConfig;
import com.moa.dto.chat.ChatHistoryResponse;
import com.moa.dto.chat.ChatResponse;
import com.moa.dto.chat.clova.ClovaStudioRequest;
import com.moa.entity.AiChattingLog;
import com.moa.entity.User;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public ChatResponse sendMessage(Long userId, String userMessage) {
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
                ragContext = new StringBuilder("참고: 과거 유사한 대화 내역\n");
                for (AiChattingLog chat : similarChats) {
                    if (!chat.getChattingId().equals(userLog.getChattingId())) {
                        ragContext.append("- ").append(chat.getChatContent()).append("\n");
                    }
                }
                log.info("유사 대화 내역 : {}", ragContext.toString());
            }


            // 6. 메시지 리스트 구성
            List<ClovaStudioRequest.Message> messages = new ArrayList<>();

            // 시스템 프롬프트 추가
            messages.add(ClovaStudioRequest.Message.builder()
                    .role("system")
                    .content(ragContext != null ? ragContext.toString() + clovaConfig.getSystemPrompt() : clovaConfig.getSystemPrompt())
                    .build());

            // 현재 사용자 메시지 추가
            messages.add(ClovaStudioRequest.Message.builder()
                    .role("user")
                    .content(userMessage)
                    .build());

            log.info("Clova Studio API 호출 - 메시지 개수: {}, RAG 활성화: {}",
                    messages.size(), !similarChats.isEmpty());

            // 7. Clova Studio API 호출
            String aiResponse = clovaStudioService.sendMessage(messages);

            // 8. JSON 파싱 (거래내역 추출)
            ChatResponse.TransactionInfo transactionInfo = extractTransactionInfo(aiResponse);

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
                    .message(aiResponse)
                    .transactionInfo(transactionInfo)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("채팅 메시지 전송 실패", e);
            throw new RuntimeException("채팅 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private ChatResponse.TransactionInfo extractTransactionInfo(String aiResponse) {
        try {
            // "JSON_{...}" 패턴 찾기
            int jsonStart = aiResponse.indexOf("JSON_{");
            if (jsonStart == -1) {
                return null; // JSON 없음
            }

            int jsonEnd = aiResponse.indexOf("}", jsonStart);
            if (jsonEnd == -1) {
                return null;
            }

            String jsonString = aiResponse.substring(jsonStart + 6, jsonEnd); // "JSON_{" 제외

            // 간단한 파싱 (정규식 사용)
            String pattern = extractField(jsonString, "Pattern");
            String content = extractField(jsonString, "Content");
            String pay = extractField(jsonString, "Pay");
            String payment = extractField(jsonString, "Payment");
            String emotion = extractField(jsonString, "Emotion");

            if (pattern == null || content == null || pay == null) {
                log.warn("JSON 파싱 실패 - 필수 필드 누락");
                return null; // 필수 필드 없음
            }

            log.info("거래내역 추출 성공 - Pattern: {}, Pay: {}", pattern, pay);

            return ChatResponse.TransactionInfo.builder()
                    .pattern(pattern)
                    .content(content)
                    .pay(pay)
                    .payment(payment)
                    .emotion(emotion)
                    .build();

        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private String extractField(String json, String fieldName) {
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
