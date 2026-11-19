package com.product.api.service;

import java.util.List;

import com.product.api.dto.in.DtoProductIn;
import com.product.api.dto.out.DtoProductListOut;
import com.product.api.dto.out.DtoProductOut;

public interface SvcProduct {

    List<DtoProductListOut> getProducts();

    DtoProductOut getProduct(Integer id);

    void createProduct(DtoProductIn in);

    void updateProduct(Integer id, DtoProductIn in);

    void enableProduct(Integer id);

    void disableProduct(Integer id);
}
