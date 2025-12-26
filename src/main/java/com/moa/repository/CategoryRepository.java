package com.moa.repository;

import com.moa.entity.Category;
import com.moa.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndType(String name, CategoryType type);

    Category findByName(String 기타);
}
