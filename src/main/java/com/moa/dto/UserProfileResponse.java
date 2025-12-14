package com.moa.dto;

import com.moa.entity.UserProfile;

public record UserProfileResponse(
        Long profileId,
        String nickName,
        boolean marketingEnable,
        boolean pushEnable,
        boolean voiceEnable,
        boolean pictureEnable
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getNickname(),
                profile.isMarketingEnable(),
                profile.isPushEnable(),
                profile.isVoiceEnable(),
                profile.isPictureEnable()
        );
    }
}
