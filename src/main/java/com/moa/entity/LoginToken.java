package com.moa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_token",
       indexes = {
           @Index(name = "IDX_LOGIN_TOKEN_USER_ID", columnList = "USER_ID"),
           @Index(name = "IDX_LOGIN_TOKEN_TOKEN", columnList = "TOKEN"),
           @Index(name = "IDX_LOGIN_TOKEN_EXPIRES_AT", columnList = "EXPIRES_AT")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class LoginToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TOKEN_ID")
    private Long tokenId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "TOKEN", length = 500, nullable = false)
    private String token;

    @Column(name = "DEVICE_ID", length = 255)
    private String deviceId;

    @Column(name = "DEVICE_NAME", length = 100)
    private String deviceName;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "FK_LOGIN_TOKEN_USER"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
}
