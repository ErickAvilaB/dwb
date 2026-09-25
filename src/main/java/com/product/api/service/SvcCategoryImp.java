package com.product.api.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.exception.ApiException;

@Service
public class SvcCategoryImp implements SvcCategory {

    private final RepoCategory repoCategory;

    public SvcCategoryImp(RepoCategory repoCategory) {
        this.repoCategory = repoCategory;
    }

    @Override
    public ResponseEntity<List<Category>> getCategories() {
        try {
            List<Category> categories = repoCategory.getCategories();

            return new ResponseEntity<>(
                    categories,
                    HttpStatus.OK);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al consultar las categorías en la base de datos");
        }
    }
}