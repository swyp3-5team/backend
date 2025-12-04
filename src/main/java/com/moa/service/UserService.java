package com.moa.service;

import com.moa.entity.User;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // User ID로 사용자 조회
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // 이메일로 사용자 조회
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByUserEmail(email);
    }

    // 신규 사용자 생성 또는 탈퇴 회원 복구
    @Transactional
    public User createUser(String userName, String userEmail, String phoneNumber) {
        // 탈퇴한 회원인지 확인
        Optional<User> existingUser = userRepository.findByUserEmail(userEmail);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 탈퇴한 회원이면 복구
            if ("DELETED".equals(user.getUserStatus())) {
                user.setUserStatus("ACTIVE");
                user.setDeletedAt(null);
                user.setLastLoginAt(LocalDateTime.now());

                // 이름이 변경되었으면 업데이트
                if (userName != null && !userName.equals(user.getUserName())) {
                    user.setUserName(userName);
                }

                return userRepository.save(user);
            }

            // 이미 활성화된 회원이면 그대로 반환
            return user;
        }

        // 완전히 새로운 회원 생성
        User newUser = User.builder()
                .userName(userName)
                .userEmail(userEmail)
                .phoneNumber(phoneNumber)
                .userGrant("FREE")
                .userStatus("ACTIVE")
                .lastLoginAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    // 사용자 정보 업데이트
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // 마지막 로그인 시간 업데이트 및 탈퇴 회원 복구
    @Transactional
    public void updateLastLoginAt(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 탈퇴한 회원이면 복구
        if ("DELETED".equals(user.getUserStatus())) {
            user.setUserStatus("ACTIVE");
            user.setDeletedAt(null);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 회원 탈퇴 (Soft Delete)
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setUserStatus("DELETED");
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // 회원 완전 삭제 (Hard Delete)
    @Transactional
    public void hardDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
    }
}
