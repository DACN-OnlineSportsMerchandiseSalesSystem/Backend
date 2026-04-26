package com.javaweb.controller;

import com.javaweb.dto.AddressDTO;
import com.javaweb.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Lấy tất cả địa chỉ của mình
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getMyAddresses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.getMyAddresses(email));
    }

    // Thêm địa chỉ mới
    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@RequestBody AddressDTO addressDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(email, addressDTO));
    }

    // Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @RequestBody AddressDTO addressDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.updateAddress(id, email, addressDTO));
    }

    // Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        addressService.deleteAddress(id, email);
        return ResponseEntity.noContent().build();
    }

    // Gắn nhãn làm Địa chỉ mặc định
    @PutMapping("/{id}/default")
    public ResponseEntity<AddressDTO> setDefaultAddress(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(addressService.setDefaultAddress(id, email));
    }
}
