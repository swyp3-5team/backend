package com.moa.dto;

public record UserProfileInitRequest(
        String nickname,
        Boolean marketingEnable
) {
}
