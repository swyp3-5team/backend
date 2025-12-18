package com.moa.entity;

import com.moa.dto.TransactionUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE transactions SET is_deleted = true WHERE transaction_id = ?")
@SQLRestriction("is_deleted = false")
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
    private LocalDateTime transactionDate;

    @Column(name = "PLACE")
    private String place;

    @Column(name = "PAYMENT_MEMO")
    private String paymentMemo;

    @Column(name = "EMOTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionEmotion emotion;

    @Column(name = "IS_DELETED")
    private boolean isDeleted = false;

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
    private User user;

    @Column(name = "CREATED_AT")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void update(TransactionUpdateRequest request, Category category) {
        this.amount = request.amount();
        this.emotion = TransactionEmotion.from(request.emotion());
        this.transactionDate = request.transactionDate();
        this.place = request.place();
        this.category = category;
        this.place = request.place();
    }
}
