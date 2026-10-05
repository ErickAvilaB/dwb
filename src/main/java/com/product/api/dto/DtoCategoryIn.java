package com.product.api.dto;

import jakarta.validation.constraints.NotNull;

public class DtoCategoryIn {

    @NotNull(message = "El nombre de la categoría es obligatorio")
    private String category;

    private Integer parentCategoryId;

    @NotNull(message = "El tag de la categoría es obligatorio")
    private String tag;

    public DtoCategoryIn() {
    }

    public DtoCategoryIn(
            String category,
            Integer parentCategoryId,
            String tag) {
        this.category = category;
        this.parentCategoryId = parentCategoryId;
        this.tag = tag;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}