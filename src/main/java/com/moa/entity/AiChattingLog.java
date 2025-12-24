package com.moa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import com.moa.config.chat.VectorType;

import java.time.LocalDateTime;

/**
 * AI 채팅 로그 엔티티
 * ERD의 AI_CHATTING_LOG 테이블에 매핑
 */
@Entity
@Table(name = "ai_chatting_log", indexes = {
    @Index(name = "IDX_AI_CHATTING_USER_ID", columnList = "USER_ID"),
    @Index(name = "IDX_AI_CHATTING_CREATED_AT", columnList = "CREATED_AT"),
    @Index(name = "IDX_AI_CHATTING_USER_CREATED", columnList = "USER_ID, CREATED_AT")
})
@Data
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class AiChattingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CHATTING_ID")
    private Long chattingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_AI_CHATTING_USER"))
    private User user;

    @Column(name = "CHAT_CONTENT", nullable = false, columnDefinition = "TEXT")
    private String chatContent;

    @Column(name = "CHAT_TYPE", nullable = false, length = 20)
    private String chatType;

    @Column(name = "EMOTION", length = 50)
    private String emotion;

    /**
     * pgvector 확장 사용
     */
    @Type(VectorType.class)
    @Column(name = "EMBEDDING_VECTOR", columnDefinition = "vector(1024)")
    private String embeddingVector;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
