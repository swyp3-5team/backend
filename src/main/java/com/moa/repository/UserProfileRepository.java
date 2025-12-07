package com.moa.repository;

import com.moa.entity.User;
import com.moa.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);
}
