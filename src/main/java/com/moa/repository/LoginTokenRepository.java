package com.moa.repository;

import com.moa.entity.LoginToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginTokenRepository extends JpaRepository<LoginToken, Long> {

    // 토큰으로 조회
    Optional<LoginToken> findByToken(String token);

    // 사용자 ID로 모든 토큰 조회
    List<LoginToken> findByUserId(Long userId);

    // 사용자 ID와 디바이스 ID로 조회
    Optional<LoginToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    // 만료된 토큰 조회
    List<LoginToken> findByExpiresAtBefore(LocalDateTime now);

    // 사용자 ID로 토큰 삭제
    void deleteByUserId(Long userId);
}
