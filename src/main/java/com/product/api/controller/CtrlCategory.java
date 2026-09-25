package com.product.api.controller;

import com.product.api.entity.Category;
import com.product.api.service.SvcCategory;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
public class CtrlCategory {

    private final SvcCategory svcCategory;

    public CtrlCategory(SvcCategory svcCategory) {
        this.svcCategory = svcCategory;
    }

    @GetMapping
    public List<Category> getCategories() {
        return svcCategory.getCategories();
    }
}