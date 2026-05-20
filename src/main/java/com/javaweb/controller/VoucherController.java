package com.javaweb.controller;

import com.javaweb.dto.VoucherDTO;
import com.javaweb.dto.VoucherRequestDTO;
import com.javaweb.service.VoucherService;
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
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher & Coupon Management", description = "Endpoints for claiming discount vouchers, validating discount codes during checkout, and administrative CRUD setups")
public class VoucherController {

    private final VoucherService voucherService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (USER)
    // ==========================================

    @GetMapping("/valid")
    @Operation(summary = "Get valid shopping vouchers", description = "Retrieve a list of all active, valid promo vouchers that customers can select and apply to their checkout.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved valid vouchers list")
    })
    public ResponseEntity<List<VoucherDTO>> getValidVouchers() {
        // Trả về các Voucher hợp lệ để User áp dụng
        return ResponseEntity.ok(voucherService.getValidVouchers());
    }

    @GetMapping
    @Operation(summary = "Validate discount voucher code", description = "Check if a specific voucher code is valid for the current cart/order value, returning the calculated discount amount.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Voucher code checked and returned validation response"),
        @ApiResponse(responseCode = "400", description = "Voucher invalid, expired, or order value threshold not met")
    })
    public ResponseEntity<VoucherDTO> checkVoucher(
            @Parameter(description = "Voucher coupon code", example = "SUMMER50", required = true)
            @RequestParam String code, 
            @Parameter(description = "Total purchase value of the order before discount", example = "100000", required = true)
            @RequestParam java.math.BigDecimal orderValue) {
        // Trả về thông tin Voucher (bao gồm số tiền giảm) nếu hợp lệ
        return ResponseEntity.ok(voucherService.checkVoucher(code, orderValue));
    }

    // ==========================================
    // NHÓM API DÀNH CHO ADMIN QUẢN LÝ
    // ==========================================

    @GetMapping("/admin")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all store vouchers", description = "Admin only. Retrieve a list of all system vouchers including expired or inactive ones.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all vouchers list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/admin/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Long id) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get voucher by ID", description = "Admin only. Retrieve detailed configuration of a specific voucher campaign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved voucher details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Voucher campaign not found with the given ID")
    })
    public ResponseEntity<VoucherDTO> getVoucherById(
            @Parameter(description = "ID of the voucher campaign", example = "1", required = true)
            @PathVariable Long id) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping("/admin")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a new voucher", description = "Admin only. Add a new voucher campaign with specified discount type, rate, rules, limits, and expiration date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Voucher campaign created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherRequestDTO requestDTO) {
        return ResponseEntity.status(201).body(voucherService.createVoucher(requestDTO));
    }

    @PutMapping("/admin/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable Long id, @RequestBody VoucherRequestDTO requestDTO) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update an existing voucher", description = "Admin only. Edit parameters, limits, or validity rules of an existing voucher campaign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Voucher campaign updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Voucher campaign not found with the given ID")
    })
    public ResponseEntity<VoucherDTO> updateVoucher(
            @Parameter(description = "ID of the voucher campaign to update", example = "1", required = true)
            @PathVariable Long id, 
            @RequestBody VoucherRequestDTO requestDTO) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        return ResponseEntity.ok(voucherService.updateVoucher(id, requestDTO));
    }

    @DeleteMapping("/admin/{id}")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
=======
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete a voucher campaign", description = "Admin only. Permanently delete a voucher campaign from the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "Voucher campaign deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Voucher campaign not found with the given ID")
    })
    public ResponseEntity<Void> deleteVoucher(
            @Parameter(description = "ID of the voucher campaign to delete", example = "1", required = true)
            @PathVariable Long id) {
>>>>>>> 0e1e55ba18976b5c12b987bc76b34759271984c4
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
