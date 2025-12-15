package com.moa.repository;

import com.moa.entity.AiChattingLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 채팅 로그 Repository
 */
@Repository
public interface AiChattingLogRepository extends JpaRepository<AiChattingLog, Long> {

    Page<AiChattingLog> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Transactional
    void deleteByUserUserId(Long userId);

    @Query(value = "SELECT DISTINCT ON (chat_content) * FROM ai_chatting_log " +
                   "WHERE user_id = :userId " +
                   "AND embedding_vector IS NOT NULL " +
                   "ORDER BY chat_content, embedding_vector <=> CAST(:embeddingVector AS vector) " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<AiChattingLog> findSimilarChats(@Param("userId") Long userId,
                                         @Param("embeddingVector") String embeddingVector,
                                         @Param("limit") int limit);
}
