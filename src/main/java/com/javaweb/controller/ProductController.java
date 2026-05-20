package com.javaweb.controller;

import com.javaweb.dto.ProductDTO;
import com.javaweb.dto.ProductRequestDTO;
import com.javaweb.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing products, searching, and personalized recommendations")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all products in the catalog. Can be optionally filtered by Category ID or Brand ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products list")
    })
    public ResponseEntity<List<ProductDTO>> getAll(
            @Parameter(description = "Filter by unique category identifier", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by unique brand identifier", example = "1")
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
    @Operation(summary = "Search products via AI assistant", description = "Query the catalog using natural language. The AI will match products based on semantic similarity.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully found matching products")
    })
    public ResponseEntity<List<ProductDTO>> searchAi(
            @Parameter(description = "Natural language search prompt", example = "giày chạy bộ màu xanh đế êm", required = true)
            @RequestParam String query) {
        return ResponseEntity.ok(productService.searchProductsAi(query));
    }
    
    @GetMapping("/top-selling")
    @Operation(summary = "Get top selling products", description = "Retrieve top selling products in the system. The limit defaults to 10.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top selling products list")
    })
    public ResponseEntity<List<ProductDTO>> getTopSellingProducts(
            @Parameter(description = "Maximum number of items to return", example = "10")
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getTopSellingProductsPublic(limit));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get personalized product recommendations", description = "Retrieve a curated list of products based on the logged-in user's rank, rank-ups, and followed category interests.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendations"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User session missing")
    })
    public ResponseEntity<List<ProductDTO>> getPersonalizedRecommendations(
            Principal principal,
            @Parameter(description = "Maximum number of recommendations to return", example = "10")
            @RequestParam(required = false, defaultValue = "10") int limit) {
        String email = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(productService.getPersonalizedRecommendations(email, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID", description = "Retrieve comprehensive details of a single product using its unique database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found with the given ID")
    })
    public ResponseEntity<ProductDTO> getById(
            @Parameter(description = "Unique database ID of the product", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new product", description = "Admin only. Add a new product to the catalog database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
    public ResponseEntity<ProductDTO> create(@RequestBody ProductRequestDTO requestDTO) {
        return ResponseEntity.status(201).body(productService.createProduct(requestDTO));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductRequestDTO requestDTO) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update an existing product", description = "Admin only. Modify details of a product using its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Product not found with the given ID")
    })
    public ResponseEntity<ProductDTO> update(
            @Parameter(description = "Unique ID of the product to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody ProductRequestDTO requestDTO) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        return ResponseEntity.ok(productService.updateProduct(id, requestDTO));
    }

    @DeleteMapping("/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a product", description = "Admin only. Permanently delete a product from the database catalog.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Product not found with the given ID")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique ID of the product to delete", example = "1", required = true)
            @PathVariable Long id) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
