package com.javaweb.controller;

import com.javaweb.dto.CategoryDTO;
import com.javaweb.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "Endpoints for managing and querying product categories")
public class CategoryController {

    private final CategoryService categoryService;

    // HTTP GET: Kéo danh sách tất cả Category (Danh mục sản phẩm)
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a list of all existing product categories in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved categories list")
    })
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Admin only. Add a new product category to the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing category", description = "Admin only. Modify details of a category using its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Category not found with the given ID")
    })
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Unique ID of the category to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Admin only. Permanently delete a category from the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Category not found with the given ID")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Unique ID of the category to delete", example = "1", required = true)
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
