package com.moa.entity;

import com.moa.dto.UserProfileInitRequest;
import com.moa.entity.conf.AiChatEnum;

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

    @Column(name = "MARKETING_ENABLE",nullable = false)
    private boolean marketingEnable;

    @Column(name = "AI_CHAT_TYPE", columnDefinition = "VARCHAR(20) DEFAULT 'EMPATH'")
    private String aiChatType;

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
        this.marketingEnable = Boolean.TRUE.equals(request.marketingEnable());
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.aiChatType = AiChatEnum.EMPATH.getText();
    }

    public static UserProfile init(User user, UserProfileInitRequest request) {
        return new UserProfile(user,request);
    }

    public void updateAiChatType(String aiChatType) {
        this.aiChatType = aiChatType;
    }

    public void updateProfile(String nickname, Boolean marketingEnable) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (marketingEnable != null) {
            this.marketingEnable = marketingEnable;
        }
    }
}
