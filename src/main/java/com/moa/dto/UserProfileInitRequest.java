package com.moa.dto;

import com.moa.entity.AddressType;
import com.moa.entity.Gender;

import java.time.LocalDate;

public record UserProfileInitRequest(
        String nickname,
        LocalDate birthDate,
        Gender gender,
        AddressType addressType,

        Boolean marketingEnable,
        Boolean pushEnable,
        Boolean voiceEnable,
        Boolean pictureEnable
) {
}
