package com.product.api.service;

import java.util.List;

import com.product.api.dto.in.DtoProductImageIn;
import com.product.api.dto.out.DtoProductImageOut;

public interface SvcProductImage {

    List<DtoProductImageOut> getImages(Integer productId);

    void createImage(Integer productId, DtoProductImageIn in);

    void deleteImage(Integer productId, Integer productImageId);
}
