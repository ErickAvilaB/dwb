package com.product.api.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.api.dto.in.DtoProductIn;
import com.product.api.dto.out.DtoProductListOut;
import com.product.api.dto.out.DtoProductOut;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.common.mapper.MapperProduct;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

    private final RepoProduct repoProduct;
    private final RepoCategory repoCategory;
    private final MapperProduct mapper;

    public SvcProductImp(RepoProduct repoProduct, RepoCategory repoCategory, MapperProduct mapper) {
        this.repoProduct = repoProduct;
        this.repoCategory = repoCategory;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProductListOut> getProducts() {
        try {
            return mapper.fromProductList(repoProduct.findAll());
        } catch (DataAccessException exception) {
            throw databaseError("consultar los productos");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DtoProductOut getProduct(Integer id) {
        try {
            return mapper.toDetail(requireProduct(id));
        } catch (DataAccessException exception) {
            throw databaseError("consultar el producto");
        }
    }

    @Override
    @Transactional
    public void createProduct(DtoProductIn in) {
        try {
            validateCategory(in.getCategoryId());
            validateDuplicates(in, null);
            repoProduct.saveAndFlush(mapper.toNewEntity(in));
        } catch (DataAccessException exception) {
            throw databaseError("registrar el producto");
        }
    }

    @Override
    @Transactional
    public void updateProduct(Integer id, DtoProductIn in) {
        try {
            Product product = requireProduct(id);
            validateCategory(in.getCategoryId());
            validateDuplicates(in, id);
            mapper.copyEditableFields(in, product); // Conserva su status original.
            repoProduct.saveAndFlush(product);
        } catch (DataAccessException exception) {
            throw databaseError("actualizar el producto");
        }
    }

    @Override
    @Transactional
    public void enableProduct(Integer id) {
        changeStatus(id, 1);
    }

    @Override
    @Transactional
    public void disableProduct(Integer id) {
        changeStatus(id, 0);
    }

    private void changeStatus(Integer id, Integer status) {
        try {
            Product product = requireProduct(id);
            product.setStatus(status);
            repoProduct.saveAndFlush(product);
        } catch (DataAccessException exception) {
            throw databaseError("cambiar el estado del producto");
        }
    }

    private Product requireProduct(Integer id) {
        return repoProduct.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "El id del producto no existe"));
    }

    private void validateCategory(Integer categoryId) {
        if (!repoCategory.existsById(categoryId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El id de categoría no existe");
        }
    }

    private void validateDuplicates(DtoProductIn in, Integer id) {
        boolean gtinExists = id == null
                ? repoProduct.existsByGtin(in.getGtin())
                : repoProduct.existsByGtinAndProductIdNot(in.getGtin(), id);

        if (gtinExists) {
            throw new ApiException(HttpStatus.CONFLICT, "El gtin del producto ya está registrado");
        }

        boolean nameExists = id == null
                ? repoProduct.existsByProductIgnoreCase(in.getProduct())
                : repoProduct.existsByProductIgnoreCaseAndProductIdNot(in.getProduct(), id);

        if (nameExists) {
            throw new ApiException(HttpStatus.CONFLICT, "El nombre del producto ya está registrado");
        }
    }

    private ApiException databaseError(String operation) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error de base de datos al " + operation);
    }
}
