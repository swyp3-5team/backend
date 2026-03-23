package com.moa.controller;

import com.moa.dto.UserNotificationCreateRequest;
import com.moa.dto.UserNotificationUpdateRequest;
import com.moa.service.UserNotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-notification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "UserNotification", description = "유저 알림 API")
public class UserNotificationController {
    private final UserNotificationService userNotificationService;

    @PostMapping
    public ResponseEntity<?> createNotification(
            @RequestBody UserNotificationCreateRequest request
    ) {

        return ResponseEntity.ok(userNotificationService.createNotification(request));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotification(
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(userNotificationService.getNotification(notificationId));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> updateNotifiaction(
            @PathVariable Long notificationId,
            @RequestBody UserNotificationUpdateRequest request
    ){
        return ResponseEntity.ok(userNotificationService.updateNotification(notificationId,request));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId
    ){
        userNotificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(
                notificationId + "삭제 완료"
        );
    }

}
