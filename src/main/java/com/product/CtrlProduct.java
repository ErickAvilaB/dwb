package com.product;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
public class CtrlProduct {

    private final List<Category> categories = List.of(
            new Category(1, "Ropa", "RP", null, 1),
            new Category(2, "Calzado", "CLZD", 1, 1)
    );

    @GetMapping
    public Category[] getCategories() {
        return categories.toArray(Category[]::new);
    }
}