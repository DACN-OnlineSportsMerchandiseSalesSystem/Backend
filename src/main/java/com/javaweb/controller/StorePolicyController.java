package com.javaweb.controller;

import com.javaweb.dto.StorePolicyDTO;
import com.javaweb.dto.StorePolicyRequestDTO;
import com.javaweb.service.StorePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class StorePolicyController {

    private final StorePolicyService storePolicyService;

    // ======= PUBLIC APIs (khách hàng xem) =======

    @GetMapping
    public ResponseEntity<List<StorePolicyDTO>> getAllActivePolicies() {
        return ResponseEntity.ok(storePolicyService.getAllActivePolicies());
    }

    @GetMapping("/{key}")
    public ResponseEntity<StorePolicyDTO> getPolicyByKey(@PathVariable String key) {
        return ResponseEntity.ok(storePolicyService.getPolicyByKey(key));
    }

    // ======= ADMIN APIs (quản trị viên) =======

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StorePolicyDTO> createPolicy(@RequestBody StorePolicyRequestDTO request) {
        return ResponseEntity.status(201).body(storePolicyService.createPolicy(request));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StorePolicyDTO> updatePolicy(
            @PathVariable String key,
            @RequestBody StorePolicyRequestDTO request) {
        return ResponseEntity.ok(storePolicyService.updatePolicy(key, request));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePolicy(@PathVariable String key) {
        storePolicyService.deletePolicy(key);
        return ResponseEntity.noContent().build();
    }
}
