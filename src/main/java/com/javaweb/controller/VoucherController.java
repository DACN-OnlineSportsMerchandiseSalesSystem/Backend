package com.javaweb.controller;

import com.javaweb.dto.VoucherDTO;
import com.javaweb.dto.VoucherRequestDTO;
import com.javaweb.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (USER)
    // ==========================================

    @GetMapping("/valid")
    public ResponseEntity<List<VoucherDTO>> getValidVouchers() {
        // Trả về các Voucher hợp lệ để User áp dụng
        return ResponseEntity.ok(voucherService.getValidVouchers());
    }

    // ==========================================
    // NHÓM API DÀNH CHO ADMIN QUẢN LÝ
    // ==========================================

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<VoucherDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherRequestDTO requestDTO) {
        return ResponseEntity.status(201).body(voucherService.createVoucher(requestDTO));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<VoucherDTO> updateVoucher(@PathVariable Long id, @RequestBody VoucherRequestDTO requestDTO) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, requestDTO));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
