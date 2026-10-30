package com.product.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.product.api.entity.ProductImage;

@Repository
public interface RepoProductImage extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductIdAndStatusOrderByProductImageIdAsc(
            Integer productId, Integer status);

    Optional<ProductImage> findByProductImageIdAndProductId(
            Integer productImageId, Integer productId);
}
