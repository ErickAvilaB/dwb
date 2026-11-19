package com.product.api.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DtoProductImageOut(
        @JsonProperty("product_image_id") Integer productImageId,
        @JsonProperty("product_id") Integer productId,
        String image,
        Integer status) {
}
