package com.moa.service;

import com.moa.dto.TransactionCreateRequest;
import com.moa.dto.TransactionResponse;
import com.moa.dto.TransactionUpdateRequest;
import com.moa.entity.Category;
import com.moa.entity.Transaction;
import com.moa.entity.TransactionEmotion;
import com.moa.entity.User;
import com.moa.exception.CategoryNotFoundException;
import com.moa.exception.TransactionNotFoundException;
import com.moa.exception.UserNotFoundException;
import com.moa.repository.CategoryRepository;
import com.moa.repository.TransactionRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("유저를 찾을 수 없습니다.")
        );
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(
                CategoryNotFoundException::new
        );


        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.amount())
                .place(request.place())
                .paymentMemo(request.paymentMemo())
                .emotion(TransactionEmotion.from(request.emotion()))
                .transactionDate(request.transactionDate())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional(readOnly=true)
    public List<TransactionResponse> getTransactionsByYearMonth(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1).atStartOfDay().toLocalDate();
        LocalDate end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX).toLocalDate();

        List<Transaction> transactionList = transactionRepository.findByUser_UserIdAndTransactionDateBetween(userId, start, end);

        return transactionList.stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest request) {
        Transaction transaction = transactionRepository.findByUser_UserIdAndId(userId, transactionId).orElseThrow(()->
                new TransactionNotFoundException("존재하지 않는 지출 기록입니다.")
        );
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(CategoryNotFoundException::new);

        transaction.update(request,category);
        Transaction updatedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(updatedTransaction);
    }

    public TransactionResponse getTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByUser_UserIdAndId(userId,transactionId).orElseThrow(
                () -> new TransactionNotFoundException("존재하지 않는 지출 기록입니다.")
        );

        return TransactionResponse.from(transaction);
    }
}
