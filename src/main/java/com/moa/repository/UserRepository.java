package com.moa.repository;

import com.moa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByUserEmail(String userEmail);

    // 상태별 사용자 목록 조회
    List<User> findByUserStatus(String userStatus);

    // 활성 사용자 목록 조회 (삭제되지 않은)
    List<User> findByDeletedAtIsNull();
}
