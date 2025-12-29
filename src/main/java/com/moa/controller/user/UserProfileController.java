package com.moa.controller.user;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.AiSettingResponse;
import com.moa.dto.UserProfileInitRequest;
import com.moa.dto.UserProfileResponse;
import com.moa.entity.conf.AiChatEnum;
import com.moa.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-profiles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UserProfile", description = "유저 Profile 설정, 조회 API")
public class UserProfileController {
    private final UserProfileService userProfileService;

    @PostMapping
    @Operation(summary = "프로필 생성", description = "사용자 정보를 바탕으로 Profile을 생성")
    public ResponseEntity<String> initProfile(
        @CurrentUserId Long userId,
        @RequestBody UserProfileInitRequest request
    ){
        userProfileService.initProfile(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("프로필 생성이 완료되었습니다.");
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

    @PutMapping
    @Operation(summary = "프로필 수정", description = "사용자 프로필을 수정합니다. null이 아닌 필드만 업데이트됩니다.")
    public ResponseEntity<UserProfileResponse> updateProfile(
        @CurrentUserId Long userId,
        @RequestBody UserProfileInitRequest request
    ){
        log.info("사용자 {} 프로필 수정 요청", userId);
        UserProfileResponse response = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ai-config")
    @Operation(summary = "AI 말투 설정", description = "AI 챗봇의 말투를 설정합니다. EMPATH:공감형, FACT:팩폭형(구현X)")
    public ResponseEntity<String> settingAiChatType(
        @CurrentUserId Long userId,
        @RequestBody AiSettingResponse aiSettingResponse
    ){
        if(aiSettingResponse == null || aiSettingResponse.aiChatType() == null || aiSettingResponse.aiChatType().isEmpty()
            || (!AiChatEnum.EMPATH.getText().equals(aiSettingResponse.aiChatType())
                && !AiChatEnum.FACT.getText().equals(aiSettingResponse.aiChatType()))) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("AI 말투 설정 값이 올바르지 않습니다. EMPATH 또는 FACT 여야 합니다.");
        }

        try {
            log.info("사용자 {} AI 말투 설정 요청: {}", userId, aiSettingResponse.aiChatType());
            userProfileService.settingAiChatType(userId, aiSettingResponse.aiChatType());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("AI 말투 설정이 완료되었습니다.");
        } catch (Exception e) {
            log.error("AI 말투 설정 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 말투 설정 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/ai-config")
    @Operation(summary = "AI 말투 설정 조회", description = "현재 설정된 AI 챗봇의 말투를 조회합니다.")
    public ResponseEntity<?> getAiChatType(
        @CurrentUserId Long userId
    ){
        try {
            log.info("사용자 {} AI 말투 설정 조회 요청", userId);
            String aiChatType = userProfileService.getAiChatType(userId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new AiSettingResponse(aiChatType));
        } catch (Exception e) {
            log.error("AI 말투 설정 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 말투 설정 조회 중 오류가 발생했습니다.");
        }
    }
}
