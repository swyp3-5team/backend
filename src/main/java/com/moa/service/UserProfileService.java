package com.moa.service;

import com.moa.dto.UserProfileInitRequest;
import com.moa.dto.UserProfileResponse;
import com.moa.entity.User;
import com.moa.entity.UserProfile;
import com.moa.exception.ProfileAlreadyExistException;
import com.moa.exception.ProfileNotFoundException;
import com.moa.exception.UserNotFoundException;
import com.moa.repository.UserProfileRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;


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

    public UserProfileResponse getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseThrow(
                () -> new ProfileNotFoundException("프로필을 찾을 수 없습니다.")
        );

        return UserProfileResponse.from(userProfile);
    }
}
