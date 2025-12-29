package com.moa.repository;

import com.moa.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    // 사용자 ID와 프로바이더로 조회
    Optional<Provider> findByUserIdAndProvider(Long userId, String provider);

    // 사용자 ID로 모든 연결된 소셜 로그인 조회
    Optional<Provider> findByUserId(Long userId);

    // 프로바이더별 사용자 목록 조회
    List<Provider> findByProvider(String provider);

    // 사용자 ID로 삭제
    void deleteByUserId(Long userId);
}
