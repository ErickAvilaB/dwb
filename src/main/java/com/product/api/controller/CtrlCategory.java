package com.product.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.product.api.dto.DtoCategoryIn;
import com.product.api.entity.Category;
import com.product.api.service.SvcCategory;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/category")
public class CtrlCategory {

    private final SvcCategory svcCategory;

    public CtrlCategory(SvcCategory svcCategory) {
        this.svcCategory = svcCategory;
    }

    @GetMapping
    public ResponseEntity<List<Category>> findAll() {
        return ResponseEntity.ok(svcCategory.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Category>> findActive() {
        return ResponseEntity.ok(svcCategory.findActive());
    }

    @GetMapping("/{id}/childs")
    public ResponseEntity<List<Category>> findChilds(
            @PathVariable Integer id) {

        return ResponseEntity.ok(svcCategory.findChilds(id));
    }

    @PostMapping
    public ResponseEntity<String> create(
            @Valid @RequestBody DtoCategoryIn in) {

        svcCategory.create(in);

        return ResponseEntity.ok(
                "La categoría ha sido registrada");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Integer id,
            @Valid @RequestBody DtoCategoryIn in) {

        svcCategory.update(in, id);

        return ResponseEntity.ok(
                "La categoría ha sido actualizada");
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<String> enable(
            @PathVariable Integer id) {

        svcCategory.enable(id);

        return ResponseEntity.ok(
                "La categoría ha sido activada");
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<String> disable(
            @PathVariable Integer id) {

        svcCategory.disable(id);

        return ResponseEntity.ok(
                "La categoría ha sido desactivada");
    }
}