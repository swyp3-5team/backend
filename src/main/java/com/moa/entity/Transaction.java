package com.moa.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
@Getter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_ID")
    private Long id;

    @Column(name = "AMOUNT", nullable = false)
    private Long amount;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "PLACE")
    private String place;

    @Column(name = "PAYMENT")
    private String payment;

    @Column(name = "PAYMENT_MEMO")
    private String paymentMemo;

    @Column(name = "EMOTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionEmotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CATEGORY_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_TRANSACTION_CATEGORY")
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_TRANSACTION_USER")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void update(Long amount, LocalDate transactionDate, String place,
                      String payment, String paymentMemo, TransactionEmotion emotion, Category category) {
        if (amount != null) {
            this.amount = amount;
        }
        if (transactionDate != null) {
            this.transactionDate = transactionDate;
        }
        if (place != null) {
            this.place = place;
        }
        if (payment != null) {
            this.payment = payment;
        }
        if (paymentMemo != null) {
            this.paymentMemo = paymentMemo;
        }
        if (emotion != null) {
            this.emotion = emotion;
        }
        if (category != null) {
            this.category = category;
        }
    }
}
