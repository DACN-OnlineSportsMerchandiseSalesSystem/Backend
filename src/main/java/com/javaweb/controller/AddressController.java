package com.javaweb.controller;

import com.javaweb.dto.AddressDTO;
import com.javaweb.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Management", description = "Endpoints for managing customer shipping addresses and defaults")
public class AddressController {

    private final AddressService addressService;

    // Lấy tất cả địa chỉ của mình
    @GetMapping
    @Operation(summary = "Get user addresses", description = "Retrieve a list of shipping addresses saved under the authenticated user's profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved saved address book list")
    })
    public ResponseEntity<List<AddressDTO>> getMyAddresses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.getMyAddresses(email));
    }

    // Thêm địa chỉ mới
    @PostMapping
    @Operation(summary = "Create a new shipping address", description = "Add a new shipping/billing address to the authenticated user's profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Address created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details")
    })
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(email, addressDTO));
    }

    // Cập nhật địa chỉ
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing address", description = "Edit shipping details of a saved address by its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Address updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "404", description = "Address entry not found or unauthorized")
    })
    public ResponseEntity<AddressDTO> updateAddress(
            @Parameter(description = "ID of the address to update", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody AddressDTO addressDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.updateAddress(id, email, addressDTO));
    }

    // Xóa địa chỉ
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an address", description = "Permanently remove a shipping address from the authenticated user's address book.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "Address entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Address entry not found or unauthorized")
    })
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "ID of the address to delete", example = "1", required = true)
            @PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        addressService.deleteAddress(id, email);
        return ResponseEntity.noContent().build();
    }

    // Gắn nhãn làm Địa chỉ mặc định
    @PutMapping("/{id}/default")
    @Operation(summary = "Set address as default", description = "Mark a saved shipping address as the default selection for checkout operations.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Default address updated successfully"),
        @ApiResponse(responseCode = "404", description = "Address entry not found")
    })
    public ResponseEntity<AddressDTO> setDefaultAddress(
            @Parameter(description = "ID of the address to set as default", example = "1", required = true)
            @PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.setDefaultAddress(id, email));
    }
}
