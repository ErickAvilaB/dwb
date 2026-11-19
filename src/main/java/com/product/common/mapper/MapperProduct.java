package com.product.common.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.product.api.dto.in.DtoProductIn;
import com.product.api.dto.out.DtoProductListOut;
import com.product.api.dto.out.DtoProductOut;
import com.product.api.entity.Product;

@Component
public class MapperProduct {

    public List<DtoProductListOut> fromProductList(List<Product> products) {
        return products.stream()
                .map(product -> new DtoProductListOut(
                        product.getProductId(),
                        product.getGtin(),
                        product.getProduct(),
                        product.getPrice(),
                        product.getStatus()))
                .toList();
    }

    public DtoProductOut toDetail(Product product) {
        return new DtoProductOut(
                product.getProductId(),
                product.getGtin(),
                product.getProduct(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCategoryId(),
                product.getStatus());
    }

    public Product toNewEntity(DtoProductIn dto) {
        Product product = new Product();
        copyEditableFields(dto, product);
        product.setStatus(1);
        return product;
    }

    public void copyEditableFields(DtoProductIn dto, Product product) {
        product.setGtin(dto.getGtin());
        product.setProduct(dto.getProduct());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategoryId(dto.getCategoryId());
    }
}
