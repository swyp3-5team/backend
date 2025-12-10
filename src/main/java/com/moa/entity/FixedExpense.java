package com.moa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "fixed_expense")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
public class FixedExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FIXED_EXPENSE_ID")
    private Long id;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "EXPENSE_NAME")
    private String name;

    @Column(name = "MEMO")
    private String memo;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    @Embedded
    private RepeatRule repeatRule;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_ID",
            foreignKey = @ForeignKey(name = "FK_FIXED_EXPENSE_USER")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CATEGORY_ID",
            foreignKey = @ForeignKey(name = "FK_FIXED_EXPENSE_CATEGORY")
    )
    private Category category;
}
