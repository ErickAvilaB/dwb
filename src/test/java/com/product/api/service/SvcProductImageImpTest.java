package com.product.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.product.api.dto.in.DtoProductImageIn;
import com.product.api.entity.ProductImage;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductImage;
import com.product.common.storage.ProductImageStorage;
import com.product.exception.ApiException;

@ExtendWith(MockitoExtension.class)
class SvcProductImageImpTest {

    @Mock RepoProduct repoProduct;
    @Mock RepoProductImage repoProductImage;
    @Mock ProductImageStorage storage;
    @InjectMocks SvcProductImageImp service;

    @Test
    void permiteRegistrarMultiplesImagenesDelMismoProducto() {
        DtoProductImageIn in = new DtoProductImageIn();
        in.setProductId(3);
        in.setImage("base64-de-ejemplo");
        when(repoProduct.existsById(3)).thenReturn(true);
        when(storage.save(anyString())).thenReturn("/product/a.png", "/product/b.png");

        service.createImage(3, in);
        service.createImage(3, in);

        ArgumentCaptor<ProductImage> captor = ArgumentCaptor.forClass(ProductImage.class);
        verify(repoProductImage, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
        List<ProductImage> images = captor.getAllValues();
        assertEquals(2, images.size());
        assertEquals("/product/a.png", images.get(0).getImage());
        assertEquals("/product/b.png", images.get(1).getImage());
        assertEquals(3, images.get(0).getProductId());
        assertEquals(3, images.get(1).getProductId());
    }

    @Test
    void rechazaBodyConOtroProductId() {
        DtoProductImageIn in = new DtoProductImageIn();
        in.setProductId(8);
        in.setImage("base64-de-ejemplo");

        ApiException exception = assertThrows(ApiException.class,
                () -> service.createImage(3, in));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(repoProductImage, never()).saveAndFlush(any(ProductImage.class));
    }

    @Test
    void eliminaRegistroYArchivoDeImagenDelProducto() {
        ProductImage image = new ProductImage();
        image.setProductImageId(10);
        image.setProductId(3);
        image.setImage("/product/ejemplo.png");

        when(repoProduct.existsById(3)).thenReturn(true);
        when(repoProductImage.findByProductImageIdAndProductId(10, 3))
                .thenReturn(Optional.of(image));

        service.deleteImage(3, 10);

        var order = org.mockito.Mockito.inOrder(repoProductImage, storage);
        order.verify(repoProductImage).delete(image);
        order.verify(repoProductImage).flush();
        order.verify(storage).delete("/product/ejemplo.png");
    }

    @Test
    void impideEliminarImagenDeOtroProducto() {
        when(repoProduct.existsById(3)).thenReturn(true);
        when(repoProductImage.findByProductImageIdAndProductId(10, 3))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> service.deleteImage(3, 10));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(storage, never()).delete(anyString());
    }
}
