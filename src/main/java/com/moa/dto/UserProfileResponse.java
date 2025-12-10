package com.moa.dto;

import com.moa.entity.AddressType;
import com.moa.entity.Gender;
import com.moa.entity.UserProfile;

import java.time.LocalDate;

public record UserProfileResponse(
        Long profileId,
        String nickName,
        LocalDate birth,
        Gender gender,
        AddressType addressType,
        boolean marketingEnable,
        boolean pushEnable,
        boolean voiceEnable,
        boolean pictureEnable
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getNickname(),
                profile.getBirthDate(),
                profile.getGender(),
                profile.getAdressType(),
                profile.isMarketingEnable(),
                profile.isPushEnable(),
                profile.isVoiceEnable(),
                profile.isPictureEnable()
        );
    }
}
