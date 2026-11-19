package com.product.api.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DtoProductImageIn {

    @JsonProperty("product_id")
    @NotNull(message = "El product_id es obligatorio")
    @Min(value = 1, message = "El product_id debe ser positivo")
    private Integer productId;

    @NotBlank(message = "La imagen es obligatoria")
    private String image;

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
