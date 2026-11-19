package com.product.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.api.dto.in.DtoProductIn;
import com.product.api.dto.out.DtoProductListOut;
import com.product.api.dto.out.DtoProductOut;
import com.product.api.service.SvcProduct;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/product")
public class CtrlProduct {

    private final SvcProduct svcProduct;

    public CtrlProduct(SvcProduct svcProduct) {
        this.svcProduct = svcProduct;
    }

    @GetMapping
    public ResponseEntity<List<DtoProductListOut>> getProducts() {
        return ResponseEntity.ok(svcProduct.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DtoProductOut> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(svcProduct.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody DtoProductIn in) {
        svcProduct.createProduct(in);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("El producto ha sido registrado");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody DtoProductIn in) {
        svcProduct.updateProduct(id, in);
        return ResponseEntity.ok("El producto ha sido actualizado");
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<String> enableProduct(@PathVariable Integer id) {
        svcProduct.enableProduct(id);
        return ResponseEntity.ok("El producto ha sido activado");
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<String> disableProduct(@PathVariable Integer id) {
        svcProduct.disableProduct(id);
        return ResponseEntity.ok("El producto ha sido desactivado");
    }
}
