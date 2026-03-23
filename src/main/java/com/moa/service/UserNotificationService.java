package com.moa.service;

import com.moa.dto.UserNotificationCreateRequest;
import com.moa.dto.UserNotificationResponse;
import com.moa.dto.UserNotificationUpdateRequest;
import com.moa.entity.UserNotification;
import com.moa.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationService {
    private final UserNotificationRepository notificationRepository;

    public UserNotificationResponse createNotification(UserNotificationCreateRequest request) {

        UserNotification userNotification = UserNotification.builder()
                .content(request.content())
                .reservedAt(request.reservedAt())
                .build();
        UserNotification savedUsernotification = notificationRepository.save(userNotification);

        return new UserNotificationResponse(
                savedUsernotification.getId(),
                savedUsernotification.getContent(),
                savedUsernotification.getReservedAt()
        );
    }

    public UserNotificationResponse getNotification(Long id){
        UserNotification userNotification = notificationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("잘못된 Id 입니다.")
        );

        return new UserNotificationResponse(
                userNotification.getId(),
                userNotification.getContent(),
                userNotification.getReservedAt()
        );
    }

    public void deleteNotification(Long id){
        UserNotification userNotification = notificationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("잘못된 ID 입니다.")
        );

        notificationRepository.delete(userNotification);
        log.info("알림 삭제 완료");
    }

    public UserNotificationResponse updateNotification(Long notificationId, UserNotificationUpdateRequest request){
        UserNotification userNotification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new IllegalArgumentException("잘못된 ID 입니다.")
        );

        userNotification.update(
                notificationId,
                request.content(),
                request.reservedAt()
        );
        UserNotification savedNotification = notificationRepository.save(userNotification);
        return new UserNotificationResponse(
                savedNotification.getId(),
                savedNotification.getContent(),
                savedNotification.getReservedAt()
        );
    }
}
