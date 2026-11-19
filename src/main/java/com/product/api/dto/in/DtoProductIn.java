package com.product.api.dto.in;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class DtoProductIn {

    @NotBlank(message = "El gtin es obligatorio")
    @Pattern(regexp = "\\d{13}", message = "El gtin debe tener exactamente 13 dígitos")
    private String gtin;

    @NotBlank(message = "El product es obligatorio")
    private String product;

    @NotNull(message = "El description es obligatorio")
    private String description;

    @NotNull(message = "El price es obligatorio")
    @DecimalMin(value = "0.0", message = "El price no puede ser negativo")
    private Float price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @JsonProperty("category_id")
    @NotNull(message = "El category_id es obligatorio")
    @Min(value = 1, message = "El category_id debe ser positivo")
    private Integer categoryId;

    public String getGtin() { return gtin; }
    public void setGtin(String gtin) { this.gtin = gtin; }
    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}
