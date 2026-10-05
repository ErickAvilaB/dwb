package com.product.api.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.api.dto.DtoCategoryIn;
import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.exception.ApiException;

@Service
public class SvcCategoryImp implements SvcCategory {

    private static final Integer STATUS_ACTIVE = 1;
    private static final Integer STATUS_INACTIVE = 0;

    private final RepoCategory repoCategory;

    public SvcCategoryImp(RepoCategory repoCategory) {
        this.repoCategory = repoCategory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        try {
            return repoCategory.findAllByOrderByCategoryAsc();
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al consultar las categorías en la base de datos");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findActive() {
        try {
            return repoCategory.findByStatusOrderByCategoryAsc(STATUS_ACTIVE);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al consultar las categorías activas");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findChilds(Integer id) {
        try {
            validateCategoryId(id);

            return repoCategory
                    .findByParentCategoryIdOrderByCategoryAsc(id);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al consultar las categorías hijas");
        }
    }

    @Override
    @Transactional
    public void create(DtoCategoryIn in) {
        try {
            validateCreateDuplicates(in);
            validateParentCategory(in.getParentCategoryId(), null);

            Category category = new Category();

            category.setCategory(in.getCategory());
            category.setTag(in.getTag());
            category.setParentCategoryId(in.getParentCategoryId());
            category.setStatus(STATUS_ACTIVE);

            repoCategory.saveAndFlush(category);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al registrar la categoría");
        }
    }

    @Override
    @Transactional
    public void update(DtoCategoryIn in, Integer id) {
        try {
            Category category = validateCategoryId(id);

            validateUpdateDuplicates(in, id);
            validateParentCategory(in.getParentCategoryId(), id);

            category.setCategory(in.getCategory());
            category.setTag(in.getTag());
            category.setParentCategoryId(in.getParentCategoryId());

            repoCategory.saveAndFlush(category);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al actualizar la categoría");
        }
    }

    @Override
    @Transactional
    public void enable(Integer id) {
        try {
            Category category = validateCategoryId(id);

            category.setStatus(STATUS_ACTIVE);

            repoCategory.saveAndFlush(category);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al activar la categoría");
        }
    }

    @Override
    @Transactional
    public void disable(Integer id) {
        try {
            Category category = validateCategoryId(id);

            if (repoCategory.existsByParentCategoryId(id)) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "No es posible desactivar una categoría que tiene categorías hijas");
            }

            category.setStatus(STATUS_INACTIVE);

            repoCategory.saveAndFlush(category);
        } catch (DataAccessException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al desactivar la categoría");
        }
    }

    private Category validateCategoryId(Integer id) {
        return repoCategory.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "El id de la categoría no existe"));
    }

    private void validateCreateDuplicates(DtoCategoryIn in) {
        if (repoCategory.existsByCategoryIgnoreCase(in.getCategory())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "El nombre de la categoría ya está registrado");
        }

        if (repoCategory.existsByTagIgnoreCase(in.getTag())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "El tag de la categoría ya está registrado");
        }
    }

    private void validateUpdateDuplicates(
            DtoCategoryIn in,
            Integer categoryId) {

        if (repoCategory.existsByCategoryIgnoreCaseAndCategoryIdNot(
                in.getCategory(),
                categoryId)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "El nombre de la categoría ya está registrado");
        }

        if (repoCategory.existsByTagIgnoreCaseAndCategoryIdNot(
                in.getTag(),
                categoryId)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "El tag de la categoría ya está registrado");
        }
    }

    private void validateParentCategory(
            Integer parentCategoryId,
            Integer categoryId) {

        if (parentCategoryId == null) {
            return;
        }

        if (parentCategoryId.equals(categoryId)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Una categoría no puede ser padre de sí misma");
        }

        Category parentCategory = repoCategory.findById(parentCategoryId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "La categoría padre no existe"));

        if (!STATUS_ACTIVE.equals(parentCategory.getStatus())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "La categoría padre debe estar activa");
        }
    }
}