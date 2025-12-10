package com.moa.controller;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.UserProfileInitRequest;
import com.moa.dto.UserProfileInitResponse;
import com.moa.dto.UserProfileResponse;
import com.moa.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UserProfile", description = "유저 Profile 설정, 조회 API")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PostMapping
    @Operation(summary = "프로필 생성", description = "사용자 정보를 바탕으로 Profile을 생성")
    public ResponseEntity<UserProfileInitResponse> initProfile(
        @CurrentUserId Long userId,
        @RequestBody UserProfileInitRequest request
    ){
        userProfileService.initProfile(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserProfileInitResponse(true));
    }

    @GetMapping("/me")
    @Operation(summary = "프로필 조회", description = "유저의 프로필을 조회")
    public ResponseEntity<UserProfileResponse> getProfile(
            @CurrentUserId Long userId
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userProfileService.getProfile(userId));
    }
}
