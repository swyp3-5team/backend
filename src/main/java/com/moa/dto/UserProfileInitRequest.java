package com.moa.dto;

public record UserProfileInitRequest(
        String nickname,
        Boolean marketingEnable,
        Boolean pushEnable,
        Boolean voiceEnable,
        Boolean pictureEnable
) {
}
