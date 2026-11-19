package com.product.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.api.dto.in.DtoProductImageIn;
import com.product.api.dto.out.DtoProductImageOut;
import com.product.api.service.SvcProductImage;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product/{id}/image")
public class CtrlProductImage {

    private final SvcProductImage svcProductImage;

    public CtrlProductImage(SvcProductImage svcProductImage) {
        this.svcProductImage = svcProductImage;
    }

    @GetMapping
    public ResponseEntity<List<DtoProductImageOut>> getProductImages(
            @PathVariable Integer id) {
        return ResponseEntity.ok(svcProductImage.getImages(id));
    }

    @PostMapping
    public ResponseEntity<String> createProductImage(
            @PathVariable Integer id,
            @Valid @RequestBody DtoProductImageIn in) {
        svcProductImage.createImage(id, in);
        return ResponseEntity.ok("La imagen ha sido registrada");
    }

    @DeleteMapping("/{productImageId}")
    public ResponseEntity<String> deleteProductImage(
            @PathVariable Integer id,
            @PathVariable Integer productImageId) {
        svcProductImage.deleteImage(id, productImageId);
        return ResponseEntity.ok("La imagen ha sido eliminada");
    }
}
