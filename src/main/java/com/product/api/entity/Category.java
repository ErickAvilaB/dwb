package com.product.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer category_id;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "tag", nullable = false, length = 100)
    private String tag;

    @Column(name = "parent_category_id")
    private Integer parentCategoryId;

    @Column(name = "status", nullable = false)
    private Integer status;

    public Category() {
    }

    public Category(
            Integer category_id,
            String category,
            String tag,
            Integer parentCategoryId,
            Integer status
    ) {
        this.category_id = category_id;
        this.category = category;
        this.tag = tag;
        this.parentCategoryId = parentCategoryId;
        this.status = status;
    }

    public Integer getCategoryId() {
        return category_id;
    }

    public void setCategoryId(Integer categoryId) {
        this.category_id = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Integer parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}