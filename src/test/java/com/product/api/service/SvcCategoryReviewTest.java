package com.product.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.exception.ApiException;

@ExtendWith(MockitoExtension.class)
class SvcCategoryReviewTest {

    @Mock
    private RepoCategory repoCategory;

    @InjectMocks
    private SvcCategoryImp service;

    @Test
    void permiteDesactivarPadreSiTodasLasHijasEstanInactivas() {
        Category parent = new Category(5, "Padre", "padre", null, 1);
        when(repoCategory.findById(5)).thenReturn(Optional.of(parent));
        when(repoCategory.existsByParentCategoryIdAndStatus(5, 1)).thenReturn(false);

        assertDoesNotThrow(() -> service.disable(5));
        assertEquals(0, parent.getStatus());
        verify(repoCategory).saveAndFlush(parent);
    }

    @Test
    void rechazaDesactivacionSiHayHijasActivas() {
        Category parent = new Category(5, "Padre", "padre", null, 1);
        when(repoCategory.findById(5)).thenReturn(Optional.of(parent));
        when(repoCategory.existsByParentCategoryIdAndStatus(5, 1)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> service.disable(5));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(1, parent.getStatus());
    }
}
