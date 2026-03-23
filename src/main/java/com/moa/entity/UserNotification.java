package com.moa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
@Getter
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long id;

    @Column(name = "RESERVED_AT", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    public void update(Long id, String content, LocalDateTime reservedAt) {
        if (id != null) this.id = id;
        if (content != null) this.content = content;
        if (reservedAt != null) this.reservedAt = reservedAt;
    }
}
