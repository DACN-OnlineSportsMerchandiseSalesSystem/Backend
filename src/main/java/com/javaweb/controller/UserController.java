package com.javaweb.controller;

import com.javaweb.dto.*;
import com.javaweb.enums.UserStatus;
import com.javaweb.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.javaweb.dto.UserRequestDTO;

//Framework
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users") // Cổng API cho Frontend gọi
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing customer profiles, personal details, interests, passwords, and administrative user controls")
public class UserController {

    private final UserService userService;

    // ==========================================
    // NHÓM API DÀNH CHO KHÁCH HÀNG (CUSTOMER) 
    // ==========================================

    @GetMapping("/my-profile")
    @Operation(summary = "Get current user profile", description = "Retrieve personal details of the logged-in customer/admin based on session token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile details retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Access token missing or invalid")
    })
    public ResponseEntity<UserDTO> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMyProfile(email));
    }

    @PutMapping("/my-profile")
    @Operation(summary = "Update current user profile", description = "Modify profile settings (names, contact details) of the logged-in user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "412", description = "Precondition failed - Authentication required")
    })
    public ResponseEntity<UserDTO> updateMyProfile(@RequestBody UserRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.updateMyProfile(email, requestDTO));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change user password", description = "Allows the logged-in user to change their authentication password safely.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password rules or wrong current password")
    })
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequestDTO requestDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.changePassword(email, requestDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/interests")
    @Operation(summary = "Get user interest categories", description = "Retrieve list of product categories that the customer followed during onboarding/profile setup.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved followed interests list")
    })
    public ResponseEntity<List<CategoryDTO>> getMyInterests() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getMyInterests(email));
    }

    @PutMapping("/interests")
    @Operation(summary = "Update followed category interests", description = "Submit a list of category IDs that the user wishes to follow. Affects personalized product recommendation outputs.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interests list updated successfully")
    })
    public ResponseEntity<Void> updateInterests(@RequestBody List<Long> categoryIds) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateInterests(email, categoryIds);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    // NHÓM API DÀNH CHO QUẢN TRỊ VIÊN (ADMIN)
    // ==========================================

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @GetMapping
    @Operation(summary = "Get all users list", description = "Admin only. Retrieve details of all registered users in the database system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all users list"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<List<UserDTO>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String roleName) {
        return ResponseEntity.ok(userService.getUsers(search, status, roleName));
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by ID", description = "Admin only. Retrieve complete details of a specific user using their unique database ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User profile not found with the given ID")
    })
    public ResponseEntity<UserDTO> getById(
            @Parameter(description = "Unique ID of the user profile", example = "1", required = true)
            @PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new user profile", description = "Admin only. Directly register a new user or administrative staff into the system database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payload details"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<UserDTO> create(@RequestBody UserRequestDTO requestDTO) {
        UserDTO newUser = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update user details by ID", description = "Admin only. Edit profiles or update specific settings of any user in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User details updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    public ResponseEntity<UserDTO> update(
            @Parameter(description = "Unique ID of the user profile to edit", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody UserRequestDTO requestDTO) {
        UserDTO updatedUser = userService.updateUser(id, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Admin only. Change account status, such as ACTIVE, INACTIVE, BANNED, or LOCKED.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    public ResponseEntity<UserDTO> updateStatus(
            @Parameter(description = "Unique ID of the user profile to edit", example = "1", required = true)
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @PatchMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Admin only. Change account role, such as CUSTOMER, ADMIN, or IT_ADMIN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User role updated successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found with the given ID")
    })
    public ResponseEntity<UserDTO> updateRole(
            @Parameter(description = "Unique ID of the user profile to edit", example = "1", required = true)
            @PathVariable Long id,
            @RequestParam String roleName) {
        return ResponseEntity.ok(userService.updateUserRole(id, roleName));
    }

    @PreAuthorize("hasAnyRole('ADMIN','IT_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete or ban user profile", description = "Admin only. Delete or deactivate a user account permanently from the database.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "240", description = "User profile deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User profile not found with the given ID")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique ID of the user to delete", example = "1", required = true)
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
