package com.javaweb.controller;

import com.javaweb.dto.BrandDTO;
import com.javaweb.service.BrandService;
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
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "Endpoints for managing and querying product brands")
public class BrandController {

    private final BrandService brandService;

    // HTTP GET: Kéo danh sách tất cả Brand (Thương hiệu)
    @GetMapping
    @Operation(summary = "Get all brands", description = "Retrieve a list of all existing product brands in the catalog.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved brands list")
    })
    public ResponseEntity<List<BrandDTO>> getAll() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @PostMapping
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new brand", description = "Admin only. Add a new product brand to the database catalog.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Brand created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
    public ResponseEntity<BrandDTO> createBrand(@RequestBody BrandDTO brandDTO) {
        return ResponseEntity.ok(brandService.createBrand(brandDTO));
    }

    @PutMapping("/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandDTO> updateBrand(@PathVariable Long id, @RequestBody BrandDTO brandDTO) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update an existing brand", description = "Admin only. Modify details of a brand using its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Brand updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Brand not found with the given ID")
    })
    public ResponseEntity<BrandDTO> updateBrand(
            @Parameter(description = "Unique ID of the brand to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody BrandDTO brandDTO) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        return ResponseEntity.ok(brandService.updateBrand(id, brandDTO));
    }

    @DeleteMapping("/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a brand", description = "Admin only. Permanently delete a brand from the database catalog.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Brand not found with the given ID")
    })
    public ResponseEntity<Void> deleteBrand(
            @Parameter(description = "Unique ID of the brand to delete", example = "1", required = true)
            @PathVariable Long id) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
