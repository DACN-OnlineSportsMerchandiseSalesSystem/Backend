package com.javaweb.controller;

import com.javaweb.dto.CreateReturnRequestDTO;
import com.javaweb.dto.ReturnRequestDTO;
import com.javaweb.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (USER)
    // ==========================================

    @PostMapping
    public ResponseEntity<ReturnRequestDTO> createReturnRequest(
            Principal principal,
            @RequestBody CreateReturnRequestDTO requestDTO) {
        
        // Demo: Giả sử User ID là 1 (Cần thay thế bằng logic lấy User ID từ Token thực tế)
        Long userId = 1L; 
        
        return ResponseEntity.status(201).body(returnService.createReturnRequest(userId, requestDTO));
    }

    @GetMapping("/my-returns")
    public ResponseEntity<List<ReturnRequestDTO>> getMyReturnRequests(Principal principal) {
        // Demo: Lấy User ID
        Long userId = 1L; 
        return ResponseEntity.ok(returnService.getReturnRequestsByUser(userId));
    }

    // ==========================================
    // NHÓM API DÀNH CHO ADMIN
    // ==========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReturnRequestDTO>> getAllReturnRequests() {
        return ResponseEntity.ok(returnService.getAllReturnRequests());
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequestDTO> processReturnRequest(
            @PathVariable Long id, 
            @RequestParam String action) { // action = "APPROVE" or "REJECT"
        
        return ResponseEntity.ok(returnService.processReturnRequest(id, action));
    }
}
