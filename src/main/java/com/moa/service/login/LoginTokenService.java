package com.moa.service.login;

import com.moa.entity.LoginToken;
import com.moa.repository.LoginTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LoginTokenService {

    private final LoginTokenRepository loginTokenRepository;

    // 토큰 생성
    @Transactional
    public LoginToken createToken(Long userId, String deviceId, String deviceName, int expiresInSeconds) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

        LoginToken loginToken = LoginToken.builder()
                .userId(userId)
                .token(token)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .expiresAt(expiresAt)
                .build();

        log.info("로그인 토큰 생성 - userId: {}, deviceId: {}", userId, deviceId);
        return loginTokenRepository.save(loginToken);
    }

    // 토큰 검증
    public Optional<LoginToken> validateToken(String token) {
        Optional<LoginToken> loginToken = loginTokenRepository.findByToken(token);

        if (loginToken.isPresent()) {
            LoginToken lt = loginToken.get();
            // 만료 확인
            if (lt.getExpiresAt() != null && lt.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("만료된 토큰 - token: {}", token);
                return Optional.empty();
            }
            return Optional.of(lt);
        }

        return Optional.empty();
    }

    // 사용자의 모든 토큰 삭제
    @Transactional
    public void deleteAllTokensByUserId(Long userId) {
        loginTokenRepository.deleteByUserId(userId);
        log.info("모든 로그인 토큰 삭제 - userId: {}", userId);
    }

    // 특정 토큰 삭제
    @Transactional
    public void deleteToken(String token) {
        loginTokenRepository.findByToken(token).ifPresent(lt -> {
            loginTokenRepository.delete(lt);
            log.info("로그인 토큰 삭제 - token: {}", token);
        });
    }
}
