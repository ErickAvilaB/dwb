package com.product.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.product.api.entity.Category;

@Repository
public interface RepoCategory extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByCategoryAsc();

    List<Category> findByStatusOrderByCategoryAsc(Integer status);

    List<Category> findByParentCategoryIdOrderByCategoryAsc(
            Integer parentCategoryId);

    boolean existsByCategoryIgnoreCase(String category);

    boolean existsByTagIgnoreCase(String tag);

    boolean existsByCategoryIgnoreCaseAndCategoryIdNot(
            String category,
            Integer categoryId);

    boolean existsByTagIgnoreCaseAndCategoryIdNot(
            String tag,
            Integer categoryId);

    boolean existsByParentCategoryId(Integer parentCategoryId);
}