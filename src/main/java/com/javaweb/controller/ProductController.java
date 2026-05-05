package com.javaweb.controller;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId) {
        
        if (categoryId != null && brandId == null) {
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
        }
        if (brandId != null && categoryId == null) {
            return ResponseEntity.ok(productService.getProductsByBrand(brandId));
        }
        if (brandId != null && categoryId != null) {
            return ResponseEntity.ok(productService.getProductsByCategoryAndBrand(categoryId, brandId));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/search-ai")
    public ResponseEntity<List<ProductDTO>> searchAi(@RequestParam String query) {
        return ResponseEntity.ok(productService.searchProductsAi(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.status(201).body(productService.createProduct(requestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
