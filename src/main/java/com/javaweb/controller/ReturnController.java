package com.javaweb.controller;

import com.javaweb.dto.CreateReturnRequestDTO;
import com.javaweb.dto.ReturnRequestDTO;
import com.javaweb.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Tag(name = "Order Return Requests", description = "Endpoints for submitting return requests, tracking user returns, and processing returns (approving/rejecting)")
public class ReturnController {

    private final ReturnService returnService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (USER)
    // ==========================================

    @PostMapping
    @Operation(summary = "Submit a return request", description = "Allows a client to submit a return request for products in a completed order. Reasons and pictures can be attached.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Return request created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload or return conditions not met")
    })
    public ResponseEntity<ReturnRequestDTO> createReturnRequest(
            Principal principal,
            @RequestBody CreateReturnRequestDTO requestDTO) {
        
        // Demo: Giả sử User ID là 1 (Cần thay thế bằng logic lấy User ID từ Token thực tế)
        Long userId = 1L; 
        
        return ResponseEntity.status(201).body(returnService.createReturnRequest(userId, requestDTO));
    }

    @GetMapping("/my-returns")
    @Operation(summary = "Get user return requests", description = "Retrieve a list of all return requests filed under the authenticated customer's account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved return requests list")
    })
    public ResponseEntity<List<ReturnRequestDTO>> getMyReturnRequests(Principal principal) {
        // Demo: Lấy User ID
        Long userId = 1L; 
        return ResponseEntity.ok(returnService.getReturnRequestsByUser(userId));
    }

    // ==========================================
    // NHÓM API DÀNH CHO ADMIN
    // ==========================================

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Get all return requests", description = "Admin only. Retrieve a comprehensive list of all return requests submitted across the store.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved return requests list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority")
    })
    public ResponseEntity<List<ReturnRequestDTO>> getAllReturnRequests() {
        return ResponseEntity.ok(returnService.getAllReturnRequests());
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Process a return request", description = "Admin only. Approve or reject a submitted return request by its database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Return request processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid action parameter"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN authority"),
        @ApiResponse(responseCode = "404", description = "Return request not found with the given ID")
    })
    public ResponseEntity<ReturnRequestDTO> processReturnRequest(
            @Parameter(description = "ID of the return request", example = "1", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Action choice: APPROVE or REJECT", example = "APPROVE", required = true)
            @RequestParam String action) { // action = "APPROVE" or "REJECT"
        
        return ResponseEntity.ok(returnService.processReturnRequest(id, action));
    }
}
