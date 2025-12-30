package com.moa.dto;

import com.moa.entity.UserProfile;

public record UserProfileResponse(
        Long profileId,
        String nickName,
        boolean marketingEnable,
        String userEmail,
        String provider
) {
    public static UserProfileResponse from(UserProfile profile, String userEmail, String provider) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getNickname(),
                profile.isMarketingEnable(),
                userEmail,
                provider
        );
    }
}
