package com.product.api.service;

import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SvcCategoryImp implements SvcCategory {

    private final RepoCategory repoCategory;

    public SvcCategoryImp(RepoCategory repoCategory) {
        this.repoCategory = repoCategory;
    }

    @Override
    public List<Category> getCategories() {
        return repoCategory.getCategories();
    }
}