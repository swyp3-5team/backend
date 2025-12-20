package com.moa.entity;

import com.moa.dto.UserProfileInitRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "user_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue
    @Column(name = "PROFILE_ID")
    private Long id;

    // 유저 정보
    @Column(name = "NICK_NAME", nullable = false, length = 30)
    private String nickname;

    // 수신동의
    @Column(name = "PUSH_ENABLE", nullable = false)
    private boolean pushEnable;

    @Column(name = "MARKETING_ENABLE",nullable = false)
    private boolean marketingEnable;

    @Column(name = "PICTURE_ENABLE",nullable = false)
    private boolean pictureEnable;

    @Column(name = "VOICE_ENABLE",nullable = false)
    private boolean voiceEnable;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_USERPROFILE_USER")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    private UserProfile(User user, UserProfileInitRequest request) {
        this.user = user;
        this.nickname = request.nickname();
        this.pushEnable = Boolean.TRUE.equals(request.pushEnable());
        this.marketingEnable = Boolean.TRUE.equals(request.marketingEnable());
        this.voiceEnable = Boolean.TRUE.equals(request.voiceEnable());
        this.pictureEnable = Boolean.TRUE.equals(request.pictureEnable());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static UserProfile init(User user, UserProfileInitRequest request) {
        return new UserProfile(user,request);
    }
}
