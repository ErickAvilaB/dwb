package com.product.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.product.api.dto.in.DtoProductIn;
import com.product.api.entity.Product;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.common.mapper.MapperProduct;

@ExtendWith(MockitoExtension.class)
class SvcProductImpTest {

    @Mock RepoProduct repoProduct;
    @Mock RepoCategory repoCategory;
    @Mock MapperProduct mapper;
    @InjectMocks SvcProductImp service;

    @Test
    void editarProductoNoModificaSuEstado() {
        Product product = new Product();
        product.setProductId(7);
        product.setStatus(0);

        DtoProductIn in = new DtoProductIn();
        in.setCategoryId(2);
        in.setGtin("7501234567890");
        in.setProduct("Teclado");

        when(repoProduct.findById(7)).thenReturn(Optional.of(product));
        when(repoCategory.existsById(2)).thenReturn(true);

        service.updateProduct(7, in);

        assertEquals(0, product.getStatus());
        verify(repoProduct).saveAndFlush(product);
    }
}
