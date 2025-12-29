package com.moa.service;

import com.moa.dto.UserProfileInitRequest;
import com.moa.dto.UserProfileResponse;
import com.moa.entity.Provider;
import com.moa.entity.User;
import com.moa.entity.UserProfile;
import com.moa.exception.ProfileAlreadyExistException;
import com.moa.exception.ProfileNotFoundException;
import com.moa.exception.UserNotFoundException;
import com.moa.repository.ProviderRepository;
import com.moa.repository.UserProfileRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;


    @Transactional
    public void initProfile(Long userId, UserProfileInitRequest request) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("유저를 찾을 수 없습니다.")
        );

        if(userProfileRepository.existsByUser(user)){
            throw new ProfileAlreadyExistException("생성된 프로필이 존재합니다.");
        }

        UserProfile userProfile = UserProfile.init(user,request);
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
    }


    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileInitRequest request) {
        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("프로필을 찾을 수 없습니다."));

        userProfile.updateProfile(
                request.nickname(),
                request.marketingEnable(),
                request.pushEnable(),
                request.voiceEnable(),
                request.pictureEnable()
        );

        log.info("사용자 {} 프로필 업데이트 완료", userId);

        // 업데이트된 프로필 반환
        Optional<Provider> providersOptional = providerRepository.findByUserId(userId);
        Optional<User> userOptional = userRepository.findByUserId(userId);

        String providerText = providersOptional.map(Provider::getProvider).orElse(null);
        String emailText = userOptional.map(User::getUserEmail).orElse(null);

        return UserProfileResponse.from(userProfile, emailText, providerText);
    }

    public UserProfileResponse getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId).orElseThrow(
                () -> new ProfileNotFoundException("프로필을 찾을 수 없습니다.")
        );

        Optional<Provider> providersOptional = providerRepository.findByUserId(userId);
        Optional<User> userOptional = userRepository.findByUserId(userId);

        String providerText = null;
        String emailText = null;
        if(providersOptional.isPresent()) {
            Provider provider = providersOptional.get();
            providerText = provider.getProvider();
        }

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            emailText = user.getUserEmail();
        }

        return UserProfileResponse.from(userProfile, emailText, providerText);
    }

    @Transactional
    public void settingAiChatType(Long userId, String aiChatType) {
        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId).orElseThrow(
                () -> new ProfileNotFoundException("프로필을 찾을 수 없습니다.")
        );

        userProfile.updateAiChatType(aiChatType);
        log.info("사용자 {} AI 말투 설정 업데이트: {}", userId, aiChatType);
    }

    public String getAiChatType(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUser_UserId(userId).orElseThrow(
                () -> new ProfileNotFoundException("프로필을 찾을 수 없습니다.")
        );

        return userProfile.getAiChatType();
    }
}
