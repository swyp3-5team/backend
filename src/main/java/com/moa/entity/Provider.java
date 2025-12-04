package com.moa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider",
       uniqueConstraints = {
           @UniqueConstraint(name = "UK_PROVIDER_USER_PROVIDER", columnNames = {"USER_ID", "PROVIDER"})
       },
       indexes = {
           @Index(name = "IDX_PROVIDER_USER_ID", columnList = "USER_ID")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROVIDER_ID")
    private Long providerId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "PROVIDER", length = 50, nullable = false)
    private String provider; // KAKAO, APPLE

    @Column(name = "TOKEN", length = 500, nullable = false)
    private String token;

    @CreationTimestamp
    @Column(name = "CONNECT_AT", nullable = false, updatable = false)
    private LocalDateTime connectAt;

    @Column(name = "TOKEN_EXPIRES_AT")
    private LocalDateTime tokenExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "FK_PROVIDER_USER"))
    private User user;
}
