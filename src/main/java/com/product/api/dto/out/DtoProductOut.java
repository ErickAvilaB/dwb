package com.product.api.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DtoProductOut(
        @JsonProperty("product_id") Integer productId,
        String gtin,
        String product,
        String description,
        Float price,
        Integer stock,
        @JsonProperty("category_id") Integer categoryId,
        Integer status) {
}
