package com.moa.entity;

import com.moa.dto.UpdateBudgetRequest;
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
@Table(name = "budget")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Builder
@Getter
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BUDGET_ID")
    private Long id;

    @Column(name = "AMOUNT" , nullable = false)
    private Long amount;

    @Column(name = "BUDGET_MEMO")
    private String memo;

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CATEGORY_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_BUDGET_CATEGORY")
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "USER_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_BUDGET_USER")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;


    public void deactivate() {
        this.isActive = false;
    }

    public void update(UpdateBudgetRequest request) {
        if(request.memo() != null) this.memo = request.memo();
        if(request.amount() != null) this.amount = request.amount();
    }
}
