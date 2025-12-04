package com.moa.service;

import com.moa.entity.Provider;
import com.moa.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProviderService {

    private final ProviderRepository providerRepository;

    // 사용자 ID와 프로바이더로 조회
    public Optional<Provider> getProviderByUserIdAndProvider(Long userId, String providerName) {
        return providerRepository.findByUserIdAndProvider(userId, providerName);
    }

    // 소셜 로그인 연결 생성 또는 업데이트
    @Transactional
    public Provider saveOrUpdateProvider(Long userId, String providerName, String token, LocalDateTime tokenExpiresAt) {
        Optional<Provider> existingProvider = providerRepository.findByUserIdAndProvider(userId, providerName);

        if (existingProvider.isPresent()) {
            // 기존 연결 - 토큰 업데이트
            Provider provider = existingProvider.get();
            provider.setToken(token);
            provider.setTokenExpiresAt(tokenExpiresAt);
            log.info("소셜 로그인 토큰 업데이트 - userId: {}, provider: {}", userId, providerName);
            return providerRepository.save(provider);
        } else {
            // 신규 연결
            Provider newProvider = Provider.builder()
                    .userId(userId)
                    .provider(providerName)
                    .token(token)
                    .tokenExpiresAt(tokenExpiresAt)
                    .build();
            log.info("소셜 로그인 연결 생성 - userId: {}, provider: {}", userId, providerName);
            return providerRepository.save(newProvider);
        }
    }

    // 소셜 로그인 연결 해제
    @Transactional
    public void deleteProvider(Long userId, String providerName) {
        Optional<Provider> provider = providerRepository.findByUserIdAndProvider(userId, providerName);
        provider.ifPresent(p -> {
            providerRepository.delete(p);
            log.info("소셜 로그인 연결 해제 - userId: {}, provider: {}", userId, providerName);
        });
    }

    // 사용자의 모든 소셜 로그인 연결 해제
    @Transactional
    public void deleteAllProvidersByUserId(Long userId) {
        providerRepository.deleteByUserId(userId);
        log.info("모든 소셜 로그인 연결 해제 - userId: {}", userId);
    }
}
