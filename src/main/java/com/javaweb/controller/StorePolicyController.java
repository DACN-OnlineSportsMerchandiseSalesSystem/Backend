package com.javaweb.controller;

import com.javaweb.dto.StorePolicyDTO;
import com.javaweb.dto.StorePolicyRequestDTO;
import com.javaweb.service.StorePolicyService;
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
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Store Policy Management", description = "Endpoints for managing store returns, shipping, warranty policies, accessible to clients and chatbot context")
public class StorePolicyController {

    private final StorePolicyService storePolicyService;

    // ======= PUBLIC APIs (khách hàng xem) =======

    @GetMapping
    @Operation(summary = "Get all active policies", description = "Retrieve a list of all currently active policies (e.g. warranty, shipping rules).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active policies list")
    })
    public ResponseEntity<List<StorePolicyDTO>> getAllActivePolicies() {
        return ResponseEntity.ok(storePolicyService.getAllActivePolicies());
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get policy by unique key", description = "Retrieve full policy contents using its unique URL slug/key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved policy details"),
        @ApiResponse(responseCode = "404", description = "Policy not found with the given key")
    })
    public ResponseEntity<StorePolicyDTO> getPolicyByKey(
            @Parameter(description = "Unique string key of the policy", example = "shipping-policy", required = true)
            @PathVariable String key) {
        return ResponseEntity.ok(storePolicyService.getPolicyByKey(key));
    }

    // ======= ADMIN APIs (quản trị viên) =======

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new store policy", description = "Admin only. Add a new policy section to the store system. Updates context for the customer chatbot immediately.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Policy created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<StorePolicyDTO> createPolicy(@RequestBody StorePolicyRequestDTO request) {
        return ResponseEntity.status(201).body(storePolicyService.createPolicy(request));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing policy", description = "Admin only. Edit policy guidelines using its unique policy key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Policy not found with the given key")
    })
    public ResponseEntity<StorePolicyDTO> updatePolicy(
            @Parameter(description = "Unique string key of the policy to update", example = "shipping-policy", required = true)
            @PathVariable String key,
            @RequestBody StorePolicyRequestDTO request) {
        return ResponseEntity.ok(storePolicyService.updatePolicy(key, request));
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a store policy", description = "Admin only. Permanently delete a store policy using its unique key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Policy deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Policy not found with the given key")
    })
    public ResponseEntity<Void> deletePolicy(
            @Parameter(description = "Unique string key of the policy to delete", example = "shipping-policy", required = true)
            @PathVariable String key) {
        storePolicyService.deletePolicy(key);
        return ResponseEntity.noContent().build();
    }
}
