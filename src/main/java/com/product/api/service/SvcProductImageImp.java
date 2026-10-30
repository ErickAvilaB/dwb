package com.product.api.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.product.api.dto.in.DtoProductImageIn;
import com.product.api.dto.out.DtoProductImageOut;
import com.product.api.entity.ProductImage;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductImage;
import com.product.common.storage.ProductImageStorage;
import com.product.exception.ApiException;

@Service
public class SvcProductImageImp implements SvcProductImage {

    private static final Integer STATUS_ACTIVE = 1;

    private final RepoProduct repoProduct;
    private final RepoProductImage repoProductImage;
    private final ProductImageStorage storage;

    public SvcProductImageImp(
            RepoProduct repoProduct,
            RepoProductImage repoProductImage,
            ProductImageStorage storage) {
        this.repoProduct = repoProduct;
        this.repoProductImage = repoProductImage;
        this.storage = storage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtoProductImageOut> getImages(Integer productId) {
        try {
            requireProduct(productId);
            return repoProductImage
                    .findByProductIdAndStatusOrderByProductImageIdAsc(productId, STATUS_ACTIVE)
                    .stream()
                    .map(image -> new DtoProductImageOut(
                            image.getProductImageId(),
                            image.getProductId(),
                            storage.read(image.getImage()),
                            image.getStatus()))
                    .toList();
        } catch (DataAccessException exception) {
            throw databaseError("consultar las imágenes");
        }
    }

    @Override
    @Transactional
    public void createImage(Integer productId, DtoProductImageIn in) {
        if (!productId.equals(in.getProductId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "El product_id del body no coincide con el de la URL");
        }

        String storedPath = null;
        try {
            requireProduct(productId);
            storedPath = storage.save(in.getImage());

            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImage(storedPath);
            image.setStatus(STATUS_ACTIVE);

            repoProductImage.saveAndFlush(image);
        } catch (DataAccessException exception) {
            if (storedPath != null) {
                storage.deleteQuietly(storedPath);
            }
            throw databaseError("registrar la imagen");
        }
    }

    @Override
    @Transactional
    public void deleteImage(Integer productId, Integer productImageId) {
        try {
            requireProduct(productId);
            ProductImage image = repoProductImage
                    .findByProductImageIdAndProductId(productImageId, productId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND, "La imagen no pertenece al producto indicado"));

            repoProductImage.delete(image);
            repoProductImage.flush();
            storage.delete(image.getImage());
        } catch (DataAccessException exception) {
            throw databaseError("eliminar la imagen");
        }
    }

    private void requireProduct(Integer productId) {
        if (!repoProduct.existsById(productId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "El id del producto no existe");
        }
    }

    private ApiException databaseError(String operation) {
        return new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error de base de datos al " + operation);
    }
}
