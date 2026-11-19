package com.product.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.product.api.entity.Product;

@Repository
public interface RepoProduct extends JpaRepository<Product, Integer> {

    boolean existsByGtin(String gtin);

    boolean existsByProductIgnoreCase(String product);

    boolean existsByGtinAndProductIdNot(String gtin, Integer productId);

    boolean existsByProductIgnoreCaseAndProductIdNot(String product, Integer productId);
}
