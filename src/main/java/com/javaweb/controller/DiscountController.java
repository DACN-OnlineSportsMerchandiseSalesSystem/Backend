package com.javaweb.controller;

import com.javaweb.dto.DiscountDTO;
import com.javaweb.dto.DiscountRequestDTO;
import com.javaweb.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
@Tag(name = "Discount Management", description = "Endpoints for administering store-wide promotions, sales campaigns, and discount schedules")
public class DiscountController {

    private final DiscountService discountService;

    // ======= PUBLIC =======

    /** Danh sách discount đang chạy (cho banner, hiển thị sale) */
    @GetMapping("/active")
    @Operation(summary = "Get active discounts", description = "Retrieve a list of all currently active discount promotions suitable for home banners or sales sections.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active promotions list")
    })
    public ResponseEntity<List<DiscountDTO>> getActiveDiscounts() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }

    // ======= ADMIN =======

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all discounts list", description = "Admin only. Retrieve a comprehensive list of all discount campaigns ever registered in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved discounts list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<List<DiscountDTO>> getAllDiscounts() {
        return ResponseEntity.ok(discountService.getAllDiscounts());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get discount by ID", description = "Admin only. Retrieve details of a specific discount campaign by its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved discount details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount campaign not found with the given ID")
    })
    public ResponseEntity<DiscountDTO> getById(
            @Parameter(description = "ID of the discount campaign", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscountById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new discount campaign", description = "Admin only. Design and schedule a new promotional/discount campaign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Discount campaign created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<DiscountDTO> create(@RequestBody DiscountRequestDTO request) {
        return ResponseEntity.status(201).body(discountService.createDiscount(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing discount campaign", description = "Admin only. Edit parameters, schedules, or rates of a discount campaign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount campaign updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount campaign not found with the given ID")
    })
    public ResponseEntity<DiscountDTO> update(
            @Parameter(description = "ID of the discount campaign to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody DiscountRequestDTO request) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a discount campaign", description = "Admin only. Permanently delete a discount campaign from the system database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Discount campaign deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Discount campaign not found with the given ID")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the discount campaign to delete", example = "1", required = true)
            @PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.noContent().build();
    }
}
